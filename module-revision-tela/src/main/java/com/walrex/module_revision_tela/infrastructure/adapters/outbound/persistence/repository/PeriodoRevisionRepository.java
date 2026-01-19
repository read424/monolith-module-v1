package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.PeriodoRevisionEntity;

import reactor.core.publisher.Mono;

/**
 * Repository para periodo_revision
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Repository
public interface PeriodoRevisionRepository extends ReactiveCrudRepository<PeriodoRevisionEntity, Integer> {

    @Query("SELECT id_periodo, month_periodo, anio_periodo, status, create_at, update_at " +
           "FROM revision_crudo.periodo_revision WHERE status = '1' LIMIT 1")
    Mono<PeriodoRevisionEntity> findActivePeriodo();
}
