package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.SessionPesajeActivaEntity;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SessionPesajeActivaRepository extends ReactiveCrudRepository<SessionPesajeActivaEntity, Integer> {

    @Query("""
        UPDATE almacenes.session_pesaje_activa AS pesaje_enabled
        SET cnt_registro = 1 + pesaje_enabled.cnt_registro,
            status = CASE WHEN 1 + pesaje_enabled.cnt_registro = pesaje_enabled.cnt_rollos THEN '0' ELSE '1' END
        WHERE status = '1'
        RETURNING status
    """)
    Mono<String> updateActiveSessionAndReturnStatus();

    Mono<SessionPesajeActivaEntity> findByIdDetOrdenIngreso(Integer idDetOrdenIngreso);

    @Modifying
    @Query("UPDATE almacenes.session_pesaje_activa SET status = '0' WHERE id = :id")
    Mono<Integer> updateStatusToCompletedById(Integer id);
}
