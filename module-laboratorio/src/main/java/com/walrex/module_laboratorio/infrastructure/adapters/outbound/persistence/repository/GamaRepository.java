package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.entity.GamaEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GamaRepository extends ReactiveCrudRepository<GamaEntity, Integer> {

    @Query("SELECT * FROM laboratorio.tbgamas ORDER BY i_orden OFFSET :offset LIMIT :limit")
    Flux<GamaEntity> findAllPaged(long offset, int limit);

    @Query("SELECT * FROM laboratorio.tbgamas WHERE status = 1 ORDER BY i_orden OFFSET :offset LIMIT :limit")
    Flux<GamaEntity> findAllActivePaged(long offset, int limit);

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM laboratorio.tbgamas
            WHERE LOWER(TRIM(no_gama)) = LOWER(TRIM(:name))
        )
        """)
    Mono<Boolean> existsByNormalizedName(String name);

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM laboratorio.tbgamas
            WHERE LOWER(TRIM(no_gama)) = LOWER(TRIM(:name))
              AND id_gama <> :id
        )
        """)
    Mono<Boolean> existsByNormalizedNameExcludingId(String name, Integer id);

    @Query("SELECT COUNT(*) FROM laboratorio.tbgamas")
    Mono<Long> countAll();

    @Query("SELECT COUNT(*) FROM laboratorio.tbgamas WHERE status = 1")
    Mono<Long> countAllActive();

    @Query("UPDATE laboratorio.tbgamas SET status = 0, updated_at = CURRENT_TIMESTAMP WHERE id_gama = :id")
    Mono<Void> logicalDelete(Integer id);
}
