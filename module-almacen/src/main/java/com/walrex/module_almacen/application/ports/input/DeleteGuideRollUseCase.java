package com.walrex.module_almacen.application.ports.input;

import reactor.core.publisher.Mono;

public interface DeleteGuideRollUseCase {
    Mono<Void> deleteGuideRoll(Integer idDetordenIngresoRollo);
}
