package com.walrex.module_articulos.application.ports.output;

import com.walrex.module_articulos.domain.model.dto.ListArticulosDataDto;
import reactor.core.publisher.Mono;

public interface ArticuloProducerOutputPort {
    Mono<Void> sendArticulosResponse(ListArticulosDataDto data, String correlationId);
}
