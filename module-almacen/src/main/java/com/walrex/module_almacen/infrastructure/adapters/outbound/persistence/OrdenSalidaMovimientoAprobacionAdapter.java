package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaAprobacionPort;

import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.ArticuloRequerimiento;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMotivoIngreso;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OrdenSalidaMovimientoAprobacionAdapter implements OrdenSalidaAprobacionPort {
    private final OrdenSalidaAprobacionPort salidaAdapter;
    private final OrdenIngresoLogisticaPort ingresoAdapter;

    public OrdenSalidaMovimientoAprobacionAdapter(
            @Qualifier("aprobacionSalida") OrdenSalidaAprobacionPort salidaAdapter,
            @Qualifier("ingresoMovimiento") OrdenIngresoLogisticaPort ingresoAdapter) {

        this.salidaAdapter = salidaAdapter;
        this.ingresoAdapter = ingresoAdapter;
    }

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> procesarAprobacionCompleta(
            AprobarSalidaRequerimiento request,
            List<ArticuloRequerimiento> productosSeleccionados) {

        log.info("🔄 Procesando movimiento entre almacenes - Orden: {}", request.getIdOrdenSalida());

        // 1️⃣ Ejecutar SALIDA del almacén origen
        return salidaAdapter.consultarYValidarOrdenParaAprobacion(request.getIdOrdenSalida())
                .flatMap(ordenSalida -> {
                    ordenSalida.setIdUsuarioEntrega(request.getIdUsuarioEntrega());
                    ordenSalida.setIdUsuarioDeclara(request.getIdUsuarioDeclara());
                    ordenSalida.setFecEntrega(request.getFecEntrega());
                    log.info("✅ Salida completada: {}", ordenSalida.getCodEgreso());

                    return ((OrdenSalidaAprobacionPersistenceAdapter) salidaAdapter)
                            .procesarDetallesSalida(productosSeleccionados, ordenSalida)
                            .flatMap(detallesProcesados -> {
                                log.info("🔍 Detalles procesados: {}", detallesProcesados.size());
                                ordenSalida.setDetalles(detallesProcesados);
                                return salidaAdapter.actualizarEstadoEntrega(ordenSalida)
                                        .flatMap(ordenConCodigo -> {
                                            log.info("✅ Estado actualizado - Código: {}", ordenConCodigo.getCodEgreso());

                                            // 2️⃣ SEGUNDO: Registrar kardex de SALIDA
                                            return Flux.fromIterable(detallesProcesados)
                                                    .flatMap(detalle ->
                                                            ((OrdenSalidaAprobacionPersistenceAdapter) salidaAdapter)
                                                                    .registrarKardexParaDetalle(detalle, ordenConCodigo))
                                                    .then(Mono.just(ordenConCodigo))
                                                    .doOnSuccess(v -> log.info("✅ Kardex de salida registrado"));
                                        })
                                        .flatMap(ordenCompleta -> {
                                            // 3️⃣ TERCERO: Crear y ejecutar INGRESO
                                            log.info("🔄 Iniciando registro de ingreso");
                                            OrdenIngreso ordenIngreso = mapearSalidaAIngreso(ordenCompleta, request);

                                            return ingresoAdapter.guardarOrdenIngresoLogistica(ordenIngreso)
                                                    .doOnSuccess(ordenIngresoGuardada ->
                                                            log.info("✅ Ingreso completado: {}", ordenIngresoGuardada.getCod_ingreso()))
                                                    .thenReturn(ordenCompleta); // ✅ Retornar la orden de salida
                                        });
                            });
                })
                .doOnSuccess(ordenFinal ->
                        log.info("🎉 Movimiento entre almacenes completado exitosamente: {}", ordenFinal.getCodEgreso()))
                .doOnError(error ->
                        log.error("❌ Error en movimiento entre almacenes: {}", error.getMessage(), error));
    }

    private OrdenIngreso mapearSalidaAIngreso(OrdenEgresoDTO ordenSalida, AprobarSalidaRequerimiento request) {
        log.debug("🔄 Mapeando salida a ingreso - Destino: {}", request.getIdAlmacenDestino());

        log.debug("🔄 mapear Salida a Ingreso: {} ", ordenSalida);
        // ✅ Mapear detalles de salida a detalles de ingreso
        List<DetalleOrdenIngreso> detallesIngreso = ordenSalida.getDetalles().stream()
                .map(this::mapearDetalleEgresoAIngreso)
                .collect(Collectors.toList());

        return OrdenIngreso.builder()
                .almacen(Almacen.builder()
                        .idAlmacen(request.getIdAlmacenDestino())
                        .build())
                .motivo(Motivo.builder()
                        .idMotivo(TypeMotivoIngreso.MOVIMIENTO_ALMACEN.getId())
                        .descMotivo(TypeMotivoIngreso.MOVIMIENTO_ALMACEN.getDescMotivo())
                        .build())
                .fechaIngreso(LocalDate.now())
                .detalles(detallesIngreso)
                .build();
    }

    /**
     * ✅ Mapea un detalle de egreso a detalle de ingreso
     */
    private DetalleOrdenIngreso mapearDetalleEgresoAIngreso(DetalleEgresoDTO detalleEgreso) {
        return DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                    .id(detalleEgreso.getArticulo().getId())
                    .stock(detalleEgreso.getArticulo().getStock())
                    .valor_conv(detalleEgreso.getArticulo().getValor_conv())
                    .is_multiplo(detalleEgreso.getArticulo().getIs_multiplo())
                    .build())
                .cantidad(BigDecimal.valueOf(detalleEgreso.getCantidad()))
                .costo(BigDecimal.valueOf(detalleEgreso.getPrecio()))
                .idUnidad(detalleEgreso.getIdUnidad())
                .idUnidadSalida(detalleEgreso.getArticulo().getIdUnidadSalida())
                .build();
    }

    // ✅ DELEGAR todos los otros métodos al adapter de salida
    public Mono<OrdenEgresoDTO> consultarYValidarOrdenParaAprobacion(Integer idOrdenSalida) {
        return null;
    }

    @Override
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        return salidaAdapter.guardarOrdenSalida(ordenSalida);
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(OrdenEgresoDTO ordenEgresoDTO) {
        return salidaAdapter.actualizarEstadoEntrega(ordenEgresoDTO);
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        return salidaAdapter.procesarSalidaPorLotes(ordenSalida);
    }
}
