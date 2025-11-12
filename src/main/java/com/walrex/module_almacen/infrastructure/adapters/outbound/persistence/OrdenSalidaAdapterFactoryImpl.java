package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaAprobacionPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class OrdenSalidaAdapterFactoryImpl implements OrdenSalidaAdapterFactory {
    private final OrdenSalidaLogisticaPort ordenSalidaLogisticaAdapter;
    private final OrdenSalidaLogisticaPort ordenSalidaTransformacionAdapter;
    private final OrdenSalidaAprobacionPort aprobacionAdapter;
    private final OrdenSalidaAprobacionPort aprobacionMovimientoAdapter;
    private final OrdenSalidaAprobacionPort aprobacionInteligenteAdapter;

    public OrdenSalidaAdapterFactoryImpl(
            OrdenSalidaLogisticaPort ordenSalidaLogisticaAdapter,
            OrdenSalidaLogisticaPort ordenSalidaTransformacionAdapter,
            OrdenSalidaAprobacionPort aprobacionAdapter,
            OrdenSalidaAprobacionPort aprobacionMovimientoAdapter,
            OrdenSalidaAprobacionPort aprobacionInteligenteAdapter
    ){
        this.ordenSalidaLogisticaAdapter=ordenSalidaLogisticaAdapter;
        this.ordenSalidaTransformacionAdapter=ordenSalidaTransformacionAdapter;
        this.aprobacionAdapter=aprobacionAdapter;
        this.aprobacionMovimientoAdapter=aprobacionMovimientoAdapter;
        this.aprobacionInteligenteAdapter=aprobacionInteligenteAdapter;
    }

    @Override
    public Mono<OrdenSalidaLogisticaPort> getAdapter(TipoOrdenSalida tipoOrden) {
        // ✅ Mantener para no romper transformacion
        log.debug("Obteniendo adaptador para tipo de orden de salida: {}", tipoOrden);

        if (tipoOrden == null) {
            log.warn("Tipo de orden de salida es null, usando adaptador por defecto");
            return Mono.just(ordenSalidaLogisticaAdapter);
        }
        switch (tipoOrden) {
            case TRANSFORMACION:
                log.debug("Usando adaptador de transformación para salida");
                return Mono.just(ordenSalidaTransformacionAdapter);
            case APPROVE_DELIVERY:
            case APPROVE_MOVEMENT:
                log.debug("Usando adaptador aprobacion de salida");
                return Mono.just((OrdenSalidaLogisticaPort) aprobacionInteligenteAdapter);
            default:
                return Mono.just(ordenSalidaLogisticaAdapter);
        }
    }

    @Override
    public Mono<OrdenSalidaAprobacionPort> getAprobacionAdapter(TipoOrdenSalida tipoOrden) {
        // ✅ SIMPLIFICAR ESTE - solo para aprobaciones
        log.debug("Obteniendo adaptador de aprobación para tipo: {}", tipoOrden);

        switch (tipoOrden) {
            case APPROVE_DELIVERY: // ✅ El adapter inteligente maneja todo
                log.debug("Usando adaptador inteligente de aprobación");
                return Mono.just(aprobacionInteligenteAdapter);
            default:
                return Mono.error(new IllegalArgumentException(
                        "Tipo de orden no soporta aprobación: " + tipoOrden));
        }
    }
}
