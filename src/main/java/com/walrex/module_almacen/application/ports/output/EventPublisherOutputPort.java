package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;

import reactor.core.publisher.Mono;

public interface EventPublisherOutputPort {
    Mono<Void> publishGuiaDevolucionEvent(GuiaRemisionResponseEventDTO dto);
}
