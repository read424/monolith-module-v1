package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaAprobacionPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

@Slf4j
public class OrdenSalidaAdapterFactoryImpl implements OrdenSalidaAdapterFactory {
    private final OrdenSalidaLogisticaPort ordenSalidaLogisticaAdapter;
    private final OrdenSalidaLogisticaPort ordenSalidaTransformacionAdapter;
    private final OrdenSalidaAprobacionPort aprobacionAdapter;

    public OrdenSalidaAdapterFactoryImpl(
            OrdenSalidaLogisticaPort ordenSalidaLogisticaAdapter,
            @Qualifier("transformacionSalida") OrdenSalidaLogisticaPort ordenSalidaTransformacionAdapter,
            @Qualifier("aprobacionSalida") OrdenSalidaAprobacionPort aprobacionAdapter

    ){
        this.ordenSalidaLogisticaAdapter=ordenSalidaLogisticaAdapter;
        this.ordenSalidaTransformacionAdapter=ordenSalidaTransformacionAdapter;
        this.aprobacionAdapter=aprobacionAdapter;
    }

    @Override
    public Mono<OrdenSalidaLogisticaPort> getAdapter(TipoOrdenSalida tipoOrden) {
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
                log.debug("Usando adaptador aprobacion de salida");
                return Mono.just(aprobacionAdapter);
            default:
                return Mono.just(ordenSalidaLogisticaAdapter);
        }
    }

    @Override
    public Mono<OrdenSalidaAprobacionPort> getAprobacionAdapter(TipoOrdenSalida tipoOrden) {
        log.debug("Obteniendo adaptador de aprobación para tipo: {}", tipoOrden);

        if (tipoOrden == TipoOrdenSalida.APPROVE_DELIVERY) {
            return Mono.just(aprobacionAdapter);
        }

        return Mono.error(new IllegalArgumentException(
                "Tipo de orden no soporta aprobación: " + tipoOrden));
    }
}
