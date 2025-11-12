package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;

import reactor.core.publisher.Mono;

public interface ActualizarComprobanteDevolucionUseCase {
    Mono<Void> actualizarComprobanteDevolucion(GuiaRemisionResponseEventDTO guiaRemisionId, String correlationId);
}
