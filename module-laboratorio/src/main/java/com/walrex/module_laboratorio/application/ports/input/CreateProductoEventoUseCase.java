package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import reactor.core.publisher.Mono;

public interface CreateProductoEventoUseCase {
    Mono<ProductoEvento> create(ProductoEvento productoEvento);
}
