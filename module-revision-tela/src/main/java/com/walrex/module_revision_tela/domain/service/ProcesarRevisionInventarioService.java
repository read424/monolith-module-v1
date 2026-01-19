package com.walrex.module_revision_tela.domain.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.walrex.module_revision_tela.domain.model.PeriodoRevision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_revision_tela.application.ports.input.ProcesarRevisionInventarioUseCase;
import com.walrex.module_revision_tela.application.ports.output.PeriodoRevisionPort;
import com.walrex.module_revision_tela.application.ports.output.RevisionInventarioPort;
import com.walrex.module_revision_tela.domain.exceptions.NoDataToProcessException;
import com.walrex.module_revision_tela.domain.exceptions.PeriodoNotActiveException;
import com.walrex.module_revision_tela.domain.model.dto.ProcesarRevisionInventarioResponse;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailIngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailRolloRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RevisionInventarioProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para procesar revisión de inventario
 * Maneja transacciones para garantizar consistencia de datos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesarRevisionInventarioService implements ProcesarRevisionInventarioUseCase {

    private final PeriodoRevisionPort periodoRevisionPort;
    private final RevisionInventarioPort revisionInventarioPort;

    @Override
    @Transactional
    public Mono<ProcesarRevisionInventarioResponse> procesarRevision(Integer idUsuario) {
        log.info("Iniciando proceso de revisión de inventario para usuario: {}", idUsuario);

        return periodoRevisionPort.obtenerPeriodoActivo()
            .switchIfEmpty(Mono.error(new PeriodoNotActiveException()))
            .flatMap(periodo -> procesarConPeriodo(periodo, idUsuario))
            .doOnSuccess(response -> log.info("Proceso completado. Ordenes procesadas: {}",
                response.totalOrdenesProcesadas()))
            .doOnError(error -> log.error("Error procesando revisión de inventario: {}",
                error.getMessage(), error));
    }

    private Mono<ProcesarRevisionInventarioResponse> procesarConPeriodo(
        PeriodoRevision periodo,
        Integer idUsuario
    ) {
        log.debug("Procesando con periodo id: {}", periodo.getIdPeriodo());

        final AtomicInteger totalOrdenes = new AtomicInteger(0);
        final AtomicInteger totalDetalles = new AtomicInteger(0);
        final AtomicInteger totalRollos = new AtomicInteger(0);

        // Forzamos la materialización en memoria para evitar el problema con groupBy
        return revisionInventarioPort.obtenerDatosRevision()
            .filter(dato -> {
                // Filtrar duplicados: cuando status_almacen es 2 o 10, excluir si id_almacen = 2
                if (dato.getStatusAlmacen() != null &&
                    (dato.getStatusAlmacen() == 2 || dato.getStatusAlmacen() == 10)) {
                    return dato.getIdAlmacen() != null && !dato.getIdAlmacen().equals(2);
                }
                return true;
            })
            .collectList()
            .flatMap(datos -> {
                if (datos.isEmpty()) {
                    return Mono.error(new NoDataToProcessException());
                }

                Map<Integer, List<RevisionInventarioProjection>> ordenesAgrupadas = agruparPorOrden(datos);

                return Flux.fromIterable(ordenesAgrupadas.entrySet())
                    .concatMap(entry -> {
                        Integer idOrdeningreso = entry.getKey();
                        List<RevisionInventarioProjection> datosOrden = entry.getValue();
                        return procesarOrden(
                            idOrdeningreso,
                            datosOrden,
                            periodo.getIdPeriodo(),
                            idUsuario != null ? idUsuario : 26,
                            totalDetalles,
                            totalRollos
                        ).doOnSuccess(orden -> totalOrdenes.incrementAndGet());
                    })
                    .then(Mono.defer(() -> {
                        int ordenes = totalOrdenes.get();
                        if (ordenes == 0) {
                            return Mono.error(new NoDataToProcessException());
                        }

                        log.info("Total órdenes procesadas: {}", ordenes);

                        return Mono.just(construirRespuesta(
                            periodo.getIdPeriodo(),
                            ordenes,
                            totalDetalles.get(),
                            totalRollos.get()
                        ));
                    }));
            });
    }

    private Map<Integer, List<RevisionInventarioProjection>> agruparPorOrden(
        List<RevisionInventarioProjection> datos
    ) {
        Map<Integer, List<RevisionInventarioProjection>> map = new HashMap<>();
        for (RevisionInventarioProjection dato : datos) {
            map.computeIfAbsent(dato.getIdOrdeningreso(), k -> new ArrayList<>()).add(dato);
        }
        return map;
    }

    private Mono<Integer> procesarOrden(
        Integer idOrdeningreso,
        List<RevisionInventarioProjection> datosOrden,
        Integer idPeriodo,
        Integer idUsuario,
        AtomicInteger totalDetalles,
        AtomicInteger totalRollos
    ) {
        log.debug("Procesando orden: {}", idOrdeningreso);

        // Obtener datos del primer registro (header)
        RevisionInventarioProjection primero = datosOrden.get(0);

        // Crear header de revisión
        IngresoRevisionEntity ingresoRevision = IngresoRevisionEntity.builder()
            .idOrdeningreso(idOrdeningreso)
            .idCliente(primero.getIdCliente())
            .fecIngreso(primero.getFecIngreso())
            .nuSerie(primero.getNuSerie())
            .nuComprobante(primero.getNuComprobante())
            .idPeriodo(idPeriodo)
            .idUsuario(idUsuario)
            .build();

        return revisionInventarioPort.guardarIngresoRevision(ingresoRevision)
            .flatMap(ingresoGuardado -> {
                Map<Integer, List<RevisionInventarioProjection>> detallesAgrupados = agruparPorDetalle(datosOrden);

                return Flux.fromIterable(detallesAgrupados.entrySet())
                    .concatMap(entry -> {
                        Integer idDetordeningreso = entry.getKey();
                        List<RevisionInventarioProjection> datosDetalle = entry.getValue();
                        return procesarDetalle(
                            idDetordeningreso,
                            datosDetalle,
                            ingresoGuardado.getIdRevision(),
                            totalDetalles,
                            totalRollos
                        );
                    })
                    .then(Mono.just(idOrdeningreso));
            })
            .onErrorResume(error -> {
                log.error("Error procesando orden {}: {}", idOrdeningreso, error.getMessage());
                return Mono.error(error);
            });
    }

    private Map<Integer, List<RevisionInventarioProjection>> agruparPorDetalle(
        List<RevisionInventarioProjection> datos
    ) {
        Map<Integer, List<RevisionInventarioProjection>> map = new HashMap<>();
        for (RevisionInventarioProjection dato : datos) {
            map.computeIfAbsent(dato.getIdDetordeningreso(), k -> new ArrayList<>()).add(dato);
        }
        return map;
    }

    private Mono<Void> procesarDetalle(
        Integer idDetordeningreso,
        List<RevisionInventarioProjection> datosDetalle,
        Integer idRevision,
        AtomicInteger totalDetalles,
        AtomicInteger totalRollos
    ) {
        log.trace("Procesando detalle: {}", idDetordeningreso);

        RevisionInventarioProjection primero = datosDetalle.get(0);

        DetailIngresoRevisionEntity detalle = DetailIngresoRevisionEntity.builder()
            .idRevision(idRevision)
            .idDetordeningreso(idDetordeningreso)
            .idArticulo(primero.getIdArticulo())
            .build();

        return revisionInventarioPort.guardarDetailIngresoRevision(detalle)
            .flatMap(detalleGuardado -> {
                totalDetalles.incrementAndGet();

                // Procesar rollos de forma secuencial para evitar deadlocks en la BD
                return Flux.fromIterable(datosDetalle)
                    .concatMap(dato -> procesarRollo(dato, detalleGuardado.getIdDetail(), totalRollos))
                    .then();
            });
    }

    private Mono<DetailRolloRevisionEntity> procesarRollo(
        RevisionInventarioProjection dato,
        Integer idDetail,
        AtomicInteger totalRollos
    ) {
        DetailRolloRevisionEntity rollo = DetailRolloRevisionEntity.builder()
            .idDetail(idDetail)
            .idDetordeningreso(dato.getIdDetordeningreso())
            .idDetordeningresopeso(dato.getIdDetordeningresopeso())
            .idPartida(dato.getIdPartida())
            .status(dato.getStatus())
            .asCrudo(dato.getAsCrudo())
            .idDetordeningresopesoAlm(dato.getIdDetordeningresopesoAlm())
            .statusAlmacen(dato.getStatusAlmacen())
            .build();

        return revisionInventarioPort.guardarDetailRolloRevision(rollo)
            .doOnSuccess(saved -> totalRollos.incrementAndGet());
    }

    private ProcesarRevisionInventarioResponse construirRespuesta(
        Integer idPeriodo,
        Integer totalOrdenes,
        Integer totalDetalles,
        Integer totalRollos
    ) {
        String mensaje = String.format(
            "Se procesaron %d órdenes, creando %d detalles y %d rollos en el periodo %d",
            totalOrdenes, totalDetalles, totalRollos, idPeriodo
        );

        return new ProcesarRevisionInventarioResponse(
            idPeriodo,
            totalOrdenes,
            totalDetalles,
            totalRollos,
            mensaje
        );
    }
}
