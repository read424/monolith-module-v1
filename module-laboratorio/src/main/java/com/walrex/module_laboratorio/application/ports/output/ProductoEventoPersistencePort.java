package com.walrex.module_laboratorio.application.ports.output;

import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoEventoPersistencePort {
    Mono<ProductoEvento> save(ProductoEvento productoEvento);
    Mono<ProductoEvento> findById(Integer id);
    Flux<ProductoEvento> findAll(String search, int page, int size, Integer status);
    Mono<Long> count(String search, Integer status);
    Mono<Boolean> existsById(Integer id);
    Mono<Boolean> existsByNombre(String nombre);
    Mono<Boolean> existsByNombreExcludingId(String nombre, Integer id);
    Mono<Void> logicalDelete(Integer id);
}
