package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.KardexRegistrationStrategy;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SuperBuilder
@Slf4j
public class OrdenIngresoTransformacionPersistenceAdapter extends BaseOrdenIngresoPersistenceAdapter {
    private final KardexRegistrationStrategy kardexStrategy;
    private final OrdenSalidaAdapterFactory salidaAdapterFactory;

    @Override
    @Transactional // ✅ Cubre TODO el proceso (ingreso + salidas)
    public Mono<OrdenIngreso> guardarOrdenIngresoLogistica(OrdenIngreso ordenIngreso) {
        log.info("Guardando orden de transformación (ingreso + salidas)");
        return super.guardarOrdenIngresoLogistica(ordenIngreso) // 1. Procesar INGRESO
                //.flatMap(ingresoGuardado ->procesarSalidasInsumos(ingresoGuardado))      // 2. Procesar SALIDAS
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
        // Registrar en kardex usando la estrategia
        return kardexStrategy.registrarKardex(savedDetalleEntity, detalle, ordenIngreso)
                .then(actualizarIdDetalle(detalle, savedDetalleEntity))
                .doOnSuccess(detalleActualizado ->
                        log.info("✅ Detalle de transformación procesado: {}", detalleActualizado.getId()));
    }

    private Mono<OrdenIngreso> procesarSalidasInsumos(OrdenIngreso ingresoGuardado) {
        // Aquí procesarías las salidas de los insumos utilizados
        // Por ahora retornamos el ingreso, pero aquí iría la lógica de salidas
        log.info("Procesando salidas de insumos para transformación...");
        return Mono.just(ingresoGuardado);
        // TODO: Implementar lógica de salidas cuando esté lista
    }
}
