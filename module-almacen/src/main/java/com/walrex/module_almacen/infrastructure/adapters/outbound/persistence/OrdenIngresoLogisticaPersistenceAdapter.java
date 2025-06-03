package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import io.r2dbc.spi.R2dbcException;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SuperBuilder
@Slf4j
public class OrdenIngresoLogisticaPersistenceAdapter  extends BaseOrdenIngresoPersistenceAdapter {
    protected final KardexRepository kardexRepository;

    @Override
    protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
            DetalleOrdenIngreso detalle,
            DetailsIngresoEntity savedDetalleEntity,
            OrdenIngreso ordenIngreso) {

        // Crear y guardar el kardex
        KardexEntity kardexEntity = crearKardexEntity(savedDetalleEntity, detalle, ordenIngreso);

        return kardexRepository.save(kardexEntity)
                .doOnSuccess(info -> log.info("✅ Información de kardex guardado: {}", info))
                .onErrorResume(ex -> manejarErroresGuardadoKardex(ex))
                // Actualizar el ID del detalle y retornar
                .then(actualizarIdDetalle(detalle, savedDetalleEntity));
    }

    // Método para crear la entidad de kardex
    protected KardexEntity crearKardexEntity(DetailsIngresoEntity detalleEntity,
                                             DetalleOrdenIngreso detalle,
                                             OrdenIngreso ordenIngreso) {
        String str_detalle = String.format("(%s) - %s", ordenIngreso.getCod_ingreso(), ordenIngreso.getMotivo().getDescMotivo());
        BigDecimal mto_total = BigDecimal.valueOf(detalleEntity.getCosto_compra() * detalleEntity.getCantidad());
        BigDecimal cantidadConvertida;
        BigDecimal total_stock;

        // Cálculos de conversión
        if (!detalle.getIdUnidad().equals(detalle.getIdUnidadSalida())) {
            BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, detalle.getArticulo().getValor_conv()));
            cantidadConvertida = detalle.getCantidad().multiply(factorConversion).setScale(6, RoundingMode.HALF_UP);
        } else {
            cantidadConvertida = detalle.getCantidad();
        }

        total_stock = cantidadConvertida.add(detalle.getArticulo().getStock()).setScale(6, RoundingMode.HALF_UP);

        // Construir la entidad
        return KardexEntity.builder()
                .tipo_movimiento(1) // 1 - ingreso 2 - salida
                .detalle(str_detalle)
                .cantidad(BigDecimal.valueOf(detalleEntity.getCantidad()))
                .costo(BigDecimal.valueOf(detalleEntity.getCosto_compra()))
                .valorTotal(mto_total)
                .fecha_movimiento(ordenIngreso.getFechaIngreso())
                .id_articulo(detalleEntity.getId_articulo())
                .id_unidad(detalleEntity.getId_unidad())
                .id_unidad_salida(detalle.getIdUnidadSalida())
                .id_almacen(ordenIngreso.getAlmacen().getIdAlmacen())
                .id_documento(ordenIngreso.getId())
                .id_detalle_documento(detalleEntity.getId().intValue())
                .saldo_actual(total_stock)
                .saldoLote(cantidadConvertida.setScale(6, RoundingMode.HALF_UP))
                .build();
    }

    // Método para manejo de errores del kardex
    protected Mono<KardexEntity> manejarErroresGuardadoKardex(Throwable ex) {
        if (ex instanceof R2dbcException) {
            String errorMsg = "Error de base de datos al guardar el kardex: " + ex.getMessage();
            log.error(errorMsg, ex);
            return Mono.error(new RuntimeException(errorMsg, ex));
        } else {
            String errorMsg = "Error no esperado al guardar el kardex: " + ex.getMessage();
            log.error(errorMsg, ex);
            return Mono.error(new RuntimeException(errorMsg, ex));
        }
    }
}
