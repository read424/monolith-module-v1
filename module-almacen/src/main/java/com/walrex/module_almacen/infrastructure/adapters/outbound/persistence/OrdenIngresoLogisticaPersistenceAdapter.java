package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.enums.TypeMovimiento;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleInventaryEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailOrdenCompraAlmacenRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleInventoryRespository;
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
    protected final DetalleInventoryRespository detalleInventoryRespository;
    protected  final DetailOrdenCompraAlmacenRepository detailOrdenCompraAlmacenRepository;

    @Override
    protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
            DetalleOrdenIngreso detalle,
            DetailsIngresoEntity savedDetalleEntity,
            OrdenIngreso ordenIngreso) {

        // ✅ Validar y actualizar saldo de orden de compra
        return validarYActualizarOrdenCompra(detalle, ordenIngreso)
                .then(Mono.defer(() -> {
                    // ✅ NUEVO: Consultar lote asociado al ingreso
                    return consultarLoteInventario(savedDetalleEntity.getId())
                            .flatMap(inventario -> {
                                // ✅ Setear el id_lote en el detalle
                                detalle.setIdLoteInventario(inventario.getIdLote().intValue());

                                // ✅ Crear y guardar kardex con el lote correcto
                                KardexEntity kardexEntity = crearKardexEntity(savedDetalleEntity, detalle, ordenIngreso);

                                return kardexRepository.save(kardexEntity)
                                        .doOnSuccess(info -> log.info("✅ Kardex guardado con lote: {}", inventario.getIdLote()))
                                        .onErrorResume(ex -> manejarErroresGuardadoKardex(ex))
                                        .then(actualizarIdDetalle(detalle, savedDetalleEntity));
                            });
                }));
    }

    /**
     * Consulta el lote de inventario asociado al detalle de ingreso
     */
    private Mono<DetalleInventaryEntity> consultarLoteInventario(Long idDetalleIngreso) {
        log.debug("🔍 Consultando lote de inventario para detalle ingreso: {}", idDetalleIngreso);

        return detalleInventoryRespository.getInventarioByDetailIngreso(idDetalleIngreso.intValue())
                .doOnNext(inventario ->
                        log.info("✅ Lote encontrado: {} para detalle ingreso: {}",
                                inventario.getIdLote(), idDetalleIngreso))
                .switchIfEmpty(Mono.error(new RuntimeException(
                        String.format("No se encontró inventario asociado al detalle de ingreso: %d", idDetalleIngreso)
                )));
    }

    // Método para crear la entidad de kardex
    protected KardexEntity crearKardexEntity(DetailsIngresoEntity detalleEntity,
                                             DetalleOrdenIngreso detalle,
                                             OrdenIngreso ordenIngreso) {
        String str_detalle = String.format("%s - (%s)", ordenIngreso.getMotivo().getDescMotivo(), ordenIngreso.getCod_ingreso());
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
        log.info("Stock Disponible: {} Total Stock {}", detalle.getArticulo().getStock(), total_stock);

        // Construir la entidad
        return KardexEntity.builder()
                .tipo_movimiento(TypeMovimiento.INGRESO_LOGISTICA.getId())
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
                .id_lote(detalle.getIdLoteInventario())
                .saldo_actual(detalle.getArticulo().getStock())
                .saldoLote(cantidadConvertida.setScale(6, RoundingMode.HALF_UP))
                .build();
    }


    private Mono<Void> validarYActualizarOrdenCompra(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
        // Si no hay orden de compra, continuar sin actualizar
        if (ordenIngreso.getIdOrdenCompra() == null) {
            log.debug("No hay orden de compra asociada, saltando actualización de saldo");
            return Mono.empty(); // ✅ Continúa al kardex
        }
        // Validar que existe idDetalleOrdenCompra
        if (detalle.getIdDetalleOrdenCompra() == null) {
            return Mono.empty(); // ✅ Continúa al kardex
        }
        log.info("✅ Buscar información en orden de compra segun Id: {}", detalle.getIdDetalleOrdenCompra().longValue());
        return detailOrdenCompraAlmacenRepository.findById(detalle.getIdDetalleOrdenCompra().longValue())
                .switchIfEmpty(Mono.error(new RuntimeException("Detalle de orden de compra no encontrado")))
                .flatMap(detalleOC -> {
                    BigDecimal cantidadSaldo=(detalleOC.getSaldo() != null) ? detalleOC.getSaldo() : BigDecimal.valueOf(detalleOC.getCantidad());
                    BigDecimal cantidadAIngresar = detalle.getCantidad();


                    if(cantidadAIngresar.compareTo(cantidadSaldo)>0){
                        return Mono.error(new RuntimeException(
                                String.format("Cantidad %.2f, excede a saldo restante: Solicitado: %.2f",
                                        cantidadAIngresar.doubleValue(), cantidadSaldo.doubleValue())
                        ));
                    }
                    // Calcular nuevo saldo
                    BigDecimal nuevoSaldo = cantidadSaldo.subtract(cantidadAIngresar).setScale(2, RoundingMode.HALF_UP);
                    return detailOrdenCompraAlmacenRepository.updateSaldoIngreso(
                                    detalle.getIdDetalleOrdenCompra().longValue(),
                                    nuevoSaldo.doubleValue()
                            )
                            .doOnSuccess(updated -> log.info("✅ Saldo actualizado para detalle OC {}: {} -> {}",
                                    detalle.getIdDetalleOrdenCompra(), cantidadSaldo, nuevoSaldo))
                            .then();
                });
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
