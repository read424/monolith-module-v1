package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

@Slf4j
public class OrdenIngresoAdapterFactoryImpl implements OrdenIngresoAdapterFactory {

    private final OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter;
    private final OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter;
    private final OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter;

    public OrdenIngresoAdapterFactoryImpl(
            OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter,
            @Qualifier("telaCruda") OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter,
            @Qualifier("transformacion") OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter
    ){
        this.ordenIngresoLogisticaAdapter=ordenIngresoLogisticaAdapter;
        this.ordenIngresoTelaCrudaAdapter=ordenIngresoTelaCrudaAdapter;
        this.ordenIngresoTransformacionAdapter=ordenIngresoTransformacionAdapter;
    }

    @Override
    public Mono<OrdenIngresoLogisticaPort> getAdapter(TipoOrdenIngreso tipoOrden) {
        if (tipoOrden == null) {
            // Por defecto usar el adaptador general
            return Mono.just(ordenIngresoLogisticaAdapter);
        }

        switch (tipoOrden) {
            case TELA_CRUDA:
                return Mono.just(ordenIngresoTelaCrudaAdapter);
            case TRANSFORMACION:
                return Mono.just(ordenIngresoTransformacionAdapter);
            case LOGISTICA_GENERAL:
            default:
                return Mono.just(ordenIngresoLogisticaAdapter);
        }
    }
}
