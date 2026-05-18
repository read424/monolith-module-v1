package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity.EtapaTinturaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EtapaTinturaRepository extends ReactiveCrudRepository<EtapaTinturaEntity, Integer> {

    Mono<EtapaTinturaEntity> save(EtapaTinturaEntity entity);

    @Query("SELECT * FROM laboratorio.etapatintura WHERE status = 1 ORDER BY id_tintura OFFSET :offset LIMIT :limit")
    Flux<EtapaTinturaEntity> findAllActive(long offset, int limit);

    Mono<EtapaTinturaEntity> findById(Integer id);

    Mono<Boolean> existsById(Integer id);
}
