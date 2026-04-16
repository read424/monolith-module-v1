package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CurvaDisenoRepository {
    Mono<CurvaDiseno> save(CurvaDiseno curvaDiseno);

    Mono<CurvaDiseno> findById(Integer id);

    Flux<CurvaDiseno> findAll(String search, int page, int size);

    Mono<Long> countAll(String search);
}
