package com.walrex.module_articulos.application.ports.input;

import com.walrex.module_articulos.domain.model.Articulo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArticuloGraphQLInputPort {
    Flux<Articulo> searchArticulos(String query, int page, int size);

    Mono<Articulo> createArticulo(Articulo articulo);
}
