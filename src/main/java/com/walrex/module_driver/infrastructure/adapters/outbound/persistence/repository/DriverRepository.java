package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity.DriverEntity;

import reactor.core.publisher.Mono;

@Repository
public interface DriverRepository extends R2dbcRepository<DriverEntity, Long> {

    @Query("SELECT * FROM ventas.tb_conductor WHERE id_tipo_doc = :idTipoDoc AND num_documento = :numDocumento AND id_conductor != :idConductor")
    Mono<DriverEntity> ValidDocumentNotExists(Integer idTipoDoc, String numDocumento, Long idConductor);

    @Query("SELECT * FROM ventas.tb_conductor WHERE id_conductor = :id AND status = '1'")
    Mono<DriverEntity> findByIdAndStatusActive(Long id);

    @Query("UPDATE ventas.tb_conductor SET status = '0', update_at = current_timestamp WHERE id_conductor = :id")
    Mono<Void> disabledByIdLogical(Long id);
}
