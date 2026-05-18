package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.PagedResponse;
import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import reactor.core.publisher.Mono;

public interface ListProductoEventoUseCase {
    Mono<PagedResponse<ProductoEvento>> listAll(String search, int page, int size, Integer status);
}
