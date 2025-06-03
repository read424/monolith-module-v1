package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.OrdenIngreso;
import reactor.core.publisher.Mono;

public interface RegistrarOrdenIngresoUseCase {
    Mono<OrdenIngreso> registrarOrdenIngreso(OrdenIngreso ordenIngreso);
}
