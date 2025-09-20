package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.walrex.module_partidas.application.ports.output.SaveSuccessOutTachoPort;
import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.OrdenIngresoCompletaProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para SaveSuccessOutTacho
 * Implementa el puerto de salida y se encarga de la comunicación con la base de
 * datos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaveSuccessOutTachoPersistenceAdapter implements SaveSuccessOutTachoPort {

    private final DetalleIngresoRepository detalleIngresoRepository;
    private final ProcesoPartidaRepository procesoPartidaRepository;
    private final AlmacenesRepository almacenesRepository;

    @Override
    public Mono<List<ItemRollo>> consultarRollosDisponibles(Integer idPartida, Integer idAlmacen) {
        log.debug("Consultando rollos disponibles para partida ID: {} en almacén ID: {}", idPartida, idAlmacen);

        return detalleIngresoRepository.findRollosByPartida(idPartida, idAlmacen)
                .collectList()
                .map(projections -> projections.stream()
                        .map(projection -> ItemRollo.builder()
                            .codRollo(projection.getCodRollo())
                            .despacho(projection.getDespacho())
                            .idAlmacen(projection.getIdAlmacen())
                            .idDetPartida(projection.getIdDetPartida())
                            .idIngresoAlmacen(projection.getIdIngresoAlmacen())
                            .idIngresopeso(projection.getIdIngresopeso())
                            .idOrdeningreso(projection.getIdOrdeningreso())
                            .idRolloIngreso(projection.getIdRolloIngreso())
                            .isParentRollo(projection.getIsParentRollo())
                            .noAlmacen(projection.getNoAlmacen())
                            .numChildRoll(projection.getNumChildRoll())
                            .pesoAcabado(projection.getPesoAcabado())
                            .pesoRollo(projection.getPesoRollo())
                            .pesoSaldo(projection.getPesoSaldo())
                            .pesoSalida(projection.getPesoSalida())
                            .status(projection.getStatus())
                            .build()
                        )
                        .collect(java.util.stream.Collectors.toList()))
                .doOnSuccess(
                        rollos -> log.info("Rollos disponibles encontrados: {} para partida ID: {} en almacén ID: {}",
                                rollos.size(), idPartida, idAlmacen))
                .doOnError(error -> log.error(
                        "Error consultando rollos disponibles para partida ID {} en almacén ID {}: {}",
                        idPartida, idAlmacen, error.getMessage()));
    }

    @Override
    public Mono<List<ProcesoPartida>> consultarProcesosPartida(Integer idPartida) {
        log.debug("Consultando procesos para partida ID: {}", idPartida);

        return procesoPartidaRepository.findProcesosByPartida(idPartida)
                .collectList()
                .map(projections -> projections.stream()
                        .map(projection -> ProcesoPartida.builder()
                                .idCliente(projection.getIdCliente())
                                .idPartida(projection.getIdPartida())
                                .idPartidaMaquina(projection.getIdPartidaMaquina())
                                .idRuta(projection.getIdRuta())
                                .idArticulo(projection.getIdArticulo())
                                .idProceso(projection.getIdProceso())
                                .idDetRuta(projection.getIdDetRuta())
                                .noProceso(projection.getNoProceso())
                                .idAlmacen(projection.getIdAlmacen())
                                .idMaquina(projection.getIdMaquina())
                                .idTipoMaquina(projection.getIdTipoMaquina())
                                .iniciado(projection.getIniciado())
                                .finalizado(projection.getFinalizado())
                                .isPendiente(projection.getIsPendiente())
                                .status(projection.getStatus())
                                .isMainProceso(projection.getIsMainProceso())
                                .descMaq(projection.getDescMaq())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .doOnSuccess(procesos -> log.info("Procesos encontrados: {} para partida ID: {}", procesos.size(),
                        idPartida))
                .doOnError(error -> log.error("Error consultando procesos para partida ID {}: {}", idPartida,
                        error.getMessage()));
    }

    @Override
    public Mono<Integer> crearOrdenIngreso(Integer idCliente, Integer idAlmacen) {
        log.debug("Creando orden de ingreso para cliente: {}, almacén: {}, comprobante: {}",
                idCliente, idAlmacen);

        return almacenesRepository.crearOrdenIngreso(idCliente, idAlmacen);
    }

    @Override
    public Mono<Integer> crearOrdenIngresoRechazo(Integer idCliente, Integer idAlmacen, Integer idMotivoRechazo, String observacion) {
        log.debug("Creando orden de ingreso para cliente: {}, almacén: {}, motivo de rechazo: {}, observación: {}",
                idCliente, idAlmacen, idMotivoRechazo, observacion);

        return almacenesRepository.crearOrdenIngresoRechazo(idCliente, idAlmacen, idMotivoRechazo, observacion);
    }

    @Override
    public Mono<Integer> crearDetalleOrdenIngreso(Integer idOrdenIngreso, Integer idArticulo, Integer idUnidad,
            BigDecimal pesoRef, String lote, Integer nuRollos, Integer idComprobante) {
        log.debug("Creando detalle de orden de ingreso para orden: {}, artículo: {}", idOrdenIngreso, idArticulo);

        return almacenesRepository.crearDetalleOrdenIngreso(idOrdenIngreso, idArticulo, idUnidad, pesoRef, lote, nuRollos,
                idComprobante);
    }

    @Override
    public Mono<Integer> crearDetallePesoOrdenIngreso(Integer idOrdenIngreso, String codRollo, BigDecimal pesoRollo,
            Integer idDetOrdenIngreso, Integer idRolloIngreso) {
        log.debug("Creando detalle de peso para orden: {}, rollo: {}", idOrdenIngreso, codRollo);

        return almacenesRepository.crearDetallePesoOrdenIngreso(idOrdenIngreso, codRollo, pesoRollo, idDetOrdenIngreso,
                idRolloIngreso);
    }

    @Override
    public Mono<Void> actualizarStatusDetallePeso(Integer idDetOrdenIngresoPeso) {
        log.debug("Actualizando status del detalle de peso ID: {}", idDetOrdenIngresoPeso);

        return almacenesRepository.actualizarStatusDetallePeso(idDetOrdenIngresoPeso);
    }

    @Override
    public Mono<Integer> getCantidadRollosOrdenIngreso(Integer idOrdenIngreso) {
        log.debug("Consultando cantidad de rollos para orden: {}", idOrdenIngreso);

        return almacenesRepository.getCantidadRollosOrdenIngreso(idOrdenIngreso);
    }

    @Override
    public Mono<Integer> deshabilitarDetalleIngreso(Integer idOrdenIngreso) {
        log.debug("Deshabilitando detalle de ingreso para orden: {}", idOrdenIngreso);

        return almacenesRepository.deshabilitarDetalleIngreso(idOrdenIngreso);
    }

    @Override
    public Mono<Integer> deshabilitarOrdenIngreso(Integer idOrdenIngreso) {
        log.debug("Deshabilitando orden de ingreso: {}", idOrdenIngreso);

        return almacenesRepository.deshabilitarOrdenIngreso(idOrdenIngreso);
    }

    @Override
    public Mono<OrdenIngresoCompletaProjection> consultarOrdenIngresoCompleta(Integer idOrdenIngreso) {
        log.debug("Consultando orden de ingreso completa: {}", idOrdenIngreso);

        return almacenesRepository.consultarOrdenIngresoCompleta(idOrdenIngreso);
    }
}
