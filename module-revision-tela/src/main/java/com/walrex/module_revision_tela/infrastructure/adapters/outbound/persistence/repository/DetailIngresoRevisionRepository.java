package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailIngresoRevisionEntity;

/**
 * Repository para detail_ingreso_revision
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Repository
public interface DetailIngresoRevisionRepository extends ReactiveCrudRepository<DetailIngresoRevisionEntity, Integer> {
}
