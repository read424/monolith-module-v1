package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.SessionPesajeActivaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.SessionPesajeActivaWithDetailProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SessionPesajeActivaRepository extends ReactiveCrudRepository<SessionPesajeActivaEntity, Integer> {

    @Query("""
        SELECT spa.*, doi.id_ordeningreso, doi.lote
        FROM almacenes.session_pesaje_activa spa
        INNER JOIN almacenes.detordeningreso doi ON doi.id_detordeningreso = spa.id_detordeningreso
        WHERE spa.status = '1'
        LIMIT 1
    """)
    Mono<SessionPesajeActivaWithDetailProjection> findActiveSessionWithDetail();

    @Query("""
        UPDATE almacenes.session_pesaje_activa AS pesaje_enabled 
        SET cnt_registro = 1 + pesaje_enabled.cnt_registro, 
            status = CASE WHEN 1 + pesaje_enabled.cnt_registro = pesaje_enabled.cnt_rollos THEN '0' ELSE '1' END 
        WHERE status = '1' 
        RETURNING status
    """)
    Mono<String> updateActiveSessionAndReturnStatus();
}
