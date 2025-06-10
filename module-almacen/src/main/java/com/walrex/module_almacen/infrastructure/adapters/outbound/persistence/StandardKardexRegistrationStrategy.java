package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.ItemKardexDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ItemKardexDTOToKardexEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import io.r2dbc.spi.R2dbcException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Slf4j
public class StandardKardexRegistrationStrategy implements KardexRegistrationStrategy {
    private final KardexRepository kardexRepository;
    private final ItemKardexDTOToKardexEntityMapper kardexMapper;

    @Override
    public Mono<Void> registrarKardex(DetailsIngresoEntity detalleEntity, DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
        String descMotivo = ordenIngreso.getMotivo() != null ? ordenIngreso.getMotivo().getDescMotivo() : "";
        String str_detalle = String.format("(%s) - %s", ordenIngreso.getCod_ingreso(), descMotivo.toUpperCase());
        BigDecimal mto_total = BigDecimal.valueOf(detalleEntity.getCosto_compra() * detalleEntity.getCantidad());
        BigDecimal cantidadConvertida;
        BigDecimal total_stock;

        // Aplicar conversión si las unidades son diferentes
        if (!detalle.getIdUnidad().equals(detalle.getIdUnidadSalida())) {
            BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, detalle.getArticulo().getValor_conv()));
            cantidadConvertida = detalle.getCantidad().multiply(factorConversion).setScale(5, RoundingMode.HALF_UP);
        } else {
            detalle.setIdUnidadSalida(detalle.getIdUnidad());
            cantidadConvertida = detalle.getCantidad();
        }

        total_stock = cantidadConvertida.add(detalle.getArticulo().getStock()).setScale(6, RoundingMode.HALF_UP);

        KardexEntity kardexEntity = KardexEntity.builder()
                .tipo_movimiento(TypeMovimiento.INGRESO_LOGISTICA.getId()) // 1 - ingreso 2 - salida
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
                .id_lote(detalle.getIdLoteInventario())
                .id_detalle_documento(detalleEntity.getId().intValue())
                .saldo_actual(total_stock)
                .saldoLote(cantidadConvertida.setScale(6, RoundingMode.HALF_UP))
                .build();

        return kardexRepository.save(kardexEntity)
                .doOnSuccess(info -> log.info("✅ Información de kardex guardado: {}", info))
                .onErrorResume(R2dbcException.class, ex -> {
                    String errorMsg = "Error de base de datos al guardar el kardex: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new RuntimeException(errorMsg, ex));
                })
                .onErrorResume(Exception.class, ex -> {
                    String errorMsg = "Error no esperado al guardar el kardex: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new RuntimeException(errorMsg, ex));
                })
                .then();
    }


    @Override
    public Mono<KardexEntity> registrarKardex(ItemKardexDTO itemKardex){
        // ✅ Usar mapper para convertir
        KardexEntity kardexEntity = kardexMapper.toEntity(itemKardex);
        log.info("kardexEntity {}: ", kardexEntity);
        return kardexRepository.save(kardexEntity)
                .doOnSuccess(savedEntity ->
                        log.info("✅ Kardex guardado con ID: {}", savedEntity.getId_kardex()))
                .onErrorResume(R2dbcException.class, ex -> {
                    String errorMsg = "Error de base de datos al guardar kardex: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new RuntimeException(errorMsg, ex));
                })
                .onErrorResume(Exception.class, ex -> {
                    String errorMsg = "Error inesperado al guardar kardex: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new RuntimeException(errorMsg, ex));
                });
    }
}
