package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoCorregirStatusEntity;

/**
 * Repository R2DBC para la tabla ingreso_corregir_status
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Repository
public interface StatusCorreccionRepository extends R2dbcRepository<IngresoCorregirStatusEntity, Integer> {
}
