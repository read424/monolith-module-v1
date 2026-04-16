package com.walrex.module_laboratorio.application.ports.output;

import com.walrex.module_laboratorio.domain.model.Receta;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecetaPersistencePort {
    Flux<Receta> findAll(String search, int page, int size);
    Mono<Long> count(String search);
    Mono<Receta> findById(Integer id);
    Mono<Boolean> existsById(Integer id);
    Mono<Receta> updateCurvaDiseno(Integer id, String curvaDiseno);
}
