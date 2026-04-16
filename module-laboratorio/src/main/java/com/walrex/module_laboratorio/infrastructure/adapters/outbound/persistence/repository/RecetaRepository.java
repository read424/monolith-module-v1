package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.projection.RecetaProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de acceso a datos para recetas.
 * No extiende ReactiveCrudRepository porque la consulta involucra
 * JOINs entre múltiples tablas/esquemas → se implementa con DatabaseClient.
 */
public interface RecetaRepository {
    Flux<RecetaProjection> findAllPaged(String search, long offset, int limit);
    Mono<Long> countAll(String search);
    Mono<RecetaProjection> findById(Integer id);
    Mono<Boolean> existsById(Integer id);
    Mono<RecetaProjection> updateCurvaDiseno(Integer id, String curvaDiseno);
}
