package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.OrdenIngreso;
import reactor.core.publisher.Mono;

public interface OrdenIngresoLogisticaPort {
    Mono<OrdenIngreso> guardarOrdenIngresoLogistica(OrdenIngreso ordenIngreso);
}
