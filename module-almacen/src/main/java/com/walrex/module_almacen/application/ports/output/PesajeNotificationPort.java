package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.PesajeDetalle;
import reactor.core.publisher.Mono;

public interface PesajeNotificationPort {
    Mono<Void> notifyWeightRegistered(PesajeDetalle pesaje);
}
