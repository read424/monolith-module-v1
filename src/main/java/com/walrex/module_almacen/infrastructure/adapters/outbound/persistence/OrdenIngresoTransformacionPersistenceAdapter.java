package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.domain.model.mapper.DetIngresoEntityToItemKardexMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleInventaryEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleInventoryRespository;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SuperBuilder
@Slf4j
public class OrdenIngresoTransformacionPersistenceAdapter extends BaseOrdenIngresoPersistenceAdapter {
    private final KardexRegistrationStrategy kardexStrategy;
    protected final DetalleInventoryRespository detalleInventoryRespository;
    private final OrdenSalidaAdapterFactory salidaAdapterFactory;
    private final DetIngresoEntityToItemKardexMapper detIngresoEntityToItemKardexMapper;

    @Override
    @Transactional // ✅ Cubre TODO el proceso (ingreso + salidas)
    public Mono<OrdenIngreso> guardarOrdenIngresoLogistica(OrdenIngreso ordenIngreso) {
        log.info("Guardando orden de transformación (ingreso + salidas)");
        return super.guardarOrdenIngresoLogistica(ordenIngreso)
                .doOnSuccess(resultado ->
                        log.info("✅ Transformación completa: ingreso {} con salidas procesadas",
                                resultado.getId()));
    }

    // Método abstracto que implementarán las subclases
    @Override
    protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
            DetalleOrdenIngreso detalle,
            DetailsIngresoEntity savedDetalleEntity,
            OrdenIngreso ordenIngreso) {
        log.error("🚨 MÉTODO EJECUTÁNDOSE - Detalle sin ID aún, savedEntity.ID: {}", savedDetalleEntity.getId());
        // Registrar en kardex usando la estrategia
        return consultarLoteInventario(savedDetalleEntity.getId())
                .flatMap(inventario->{
                    detalle.setIdLoteInventario(inventario.getIdLote().intValue());

                    ItemKardexDTO itemKardex = detIngresoEntityToItemKardexMapper.toItemKardex(savedDetalleEntity);
                    completarInformacionKardex(itemKardex, detalle, savedDetalleEntity, ordenIngreso, inventario);

                    return kardexStrategy.registrarKardex(itemKardex)
                            .doOnSuccess(kardexGuardado -> log.info("✅ Kardex registrado con ID: {}, item: {}", kardexGuardado.getId_kardex(), kardexGuardado))
                            .doOnError(error -> log.error("❌ Error registrando kardex: {}", error.getMessage()))
                            .then(actualizarIdDetalle(detalle, savedDetalleEntity));
                })
                .doOnSuccess(detalleActualizado ->
                        log.info("✅ Detalle de transformación procesado: {}", detalleActualizado.getId()));
    }

    /**
     * Completa la información faltante en ItemKardexDTO
     */
    private void completarInformacionKardex(ItemKardexDTO itemKardex,
                                            DetalleOrdenIngreso detalle,
                                            DetailsIngresoEntity savedDetalleEntity,
                                            OrdenIngreso ordenIngreso,
                                            DetalleInventaryEntity inventario) {

        // ✅ Información del tipo de movimiento y fecha
        itemKardex.setTypeKardex(TypeMovimiento.INGRESO_LOGISTICA.getId());
        itemKardex.setFechaMovimiento(ordenIngreso.getFechaIngreso());
        itemKardex.setIdLote(inventario.getIdLote().intValue());

        // ✅ Descripción
        String descMotivo = ordenIngreso.getMotivo() != null ? ordenIngreso.getMotivo().getDescMotivo() : "";
        String descripcion = String.format("%s - (%s)", descMotivo.toUpperCase(), ordenIngreso.getCod_ingreso());
        itemKardex.setDescripcion(descripcion);

        // ✅ Cantidad y valores
        BigDecimal valorTotal = inventario.getCostoCompra().multiply(BigDecimal.valueOf(inventario.getCantidad())).setScale(6, RoundingMode.HALF_UP);
        itemKardex.setValorTotal(valorTotal);

        // ✅ Unidades
        itemKardex.setIdUnidadSalida(detalle.getIdUnidadSalida());

        // ✅ Almacén
        itemKardex.setIdAlmacen(ordenIngreso.getAlmacen().getIdAlmacen());


        // ✅ Saldos (aplicar lógica de conversión si es necesario)
        itemKardex.setSaldoStock(detalle.getArticulo().getStock());
        itemKardex.setSaldoLote(BigDecimal.valueOf(inventario.getCantidad()));

        log.debug("✅ ItemKardexDTO completado: {}", itemKardex);
    }

    /**
     * Consulta el lote de inventario asociado al detalle de ingreso
     */
    private Mono<DetalleInventaryEntity> consultarLoteInventario(Long idDetalleIngreso) {
        log.debug("🔍 Consultando lote de inventario para detalle ingreso: {}", idDetalleIngreso);

        return detalleInventoryRespository.getInventarioByDetailIngreso(idDetalleIngreso.intValue())
                .doOnNext(inventario ->
                        log.info("✅ Lote encontrado: {} para detalle ingreso: {}",
                                inventario, idDetalleIngreso))
                .switchIfEmpty(Mono.error(new RuntimeException(
                        String.format("No se encontró inventario asociado al detalle de ingreso: %d", idDetalleIngreso)
                )));
    }
}
