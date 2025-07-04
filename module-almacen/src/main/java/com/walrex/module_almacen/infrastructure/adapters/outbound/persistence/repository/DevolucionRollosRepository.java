package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionRollosEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para DevolucionRollosEntity
 */
@Repository
public interface DevolucionRollosRepository extends R2dbcRepository<DevolucionRollosEntity, Long> {

    /**
     * Verificar si un rollo de ingreso ya fue devuelto
     */
    Mono<Boolean> existsByIdDetOrdenIngresoPeso(Integer idDetOrdenIngresoPeso);

    /**
     * Buscar devoluciones por ID de detalle de orden de salida peso
     */
    Flux<DevolucionRollosEntity> findByIdDetOrdenSalidaPeso(Long idDetOrdenSalidaPeso);

    /**
     * Buscar devoluciones por ID de detalle de orden de ingreso peso
     */
    Mono<DevolucionRollosEntity> findByIdDetOrdenIngresoPeso(Integer idDetOrdenIngresoPeso);
}