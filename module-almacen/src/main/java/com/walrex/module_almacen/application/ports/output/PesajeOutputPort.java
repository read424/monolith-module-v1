package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.PesajeDetalle;
import reactor.core.publisher.Mono;

public interface PesajeOutputPort {
    Mono<PesajeDetalle> findActiveSessionWithDetail();
    Mono<PesajeDetalle> saveWeight(PesajeDetalle pesaje, Integer idDetOrdenIngreso);
    Mono<String> updateSessionState(Integer sessionId);
}
