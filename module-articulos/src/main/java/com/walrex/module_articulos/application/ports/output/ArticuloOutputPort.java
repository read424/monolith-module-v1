package com.walrex.module_articulos.application.ports.output;

import com.walrex.module_articulos.domain.model.Articulo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ArticuloOutputPort {
    Mono<Articulo> searchByCodeArticulo(String codigo);
    Flux<Articulo> findByNombreLikeIgnoreCaseOrderByNombre(String query, int page, int size);
    Flux<Articulo> findByNombreLikeIgnoreCaseAndFamily(String query, int size, int offset, Integer idTipoProducto);
    Mono<Articulo> save(Articulo articulo);
}
