package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.MotivoDevolucionEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para MotivoDevolucionEntity
 * Maneja operaciones CRUD para almacenes.tbmotivos_devoluciones
 */
@Repository
public interface MotivoDevolucionRepository extends R2dbcRepository<MotivoDevolucionEntity, Long> {

    /**
     * Buscar motivos activos ordenados por descripción
     */
    @Query("SELECT * FROM almacenes.tbmotivos_devoluciones WHERE status = 1 ORDER BY descripcion")
    Flux<MotivoDevolucionEntity> findAllActive();

    /**
     * Buscar por descripción que contenga el texto (case insensitive)
     */
    @Query("SELECT * FROM almacenes.tbmotivos_devoluciones WHERE UPPER(descripcion) LIKE UPPER('%' || :texto || '%') AND status = 1 ORDER BY descripcion")
    Flux<MotivoDevolucionEntity> findByDescripcionContains(String texto);

    /**
     * Verificar si existe un motivo con la misma descripción
     */
    @Query("SELECT COUNT(*) > 0 FROM almacenes.tbmotivos_devoluciones WHERE UPPER(descripcion) = UPPER(:descripcion) AND status = 1")
    Mono<Boolean> existsByDescripcion(String descripcion);
} 