package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailOrdenSalidaPesoEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para DetailOrdenSalidaPesoEntity
 * Maneja operaciones CRUD para almacenes.detordensalidapeso
 */
@Repository
public interface DetailOrdenSalidaPesoRepository extends ReactiveCrudRepository<DetailOrdenSalidaPesoEntity, Long> {

    /**
     * Buscar detalles de peso por ID de orden de salida
     */
    Flux<DetailOrdenSalidaPesoEntity> findByIdOrdenSalida(Long idOrdenSalida);

    /**
     * Buscar por código de rollo
     */
    Mono<DetailOrdenSalidaPesoEntity> findByCodRollo(String codRollo);

    /**
     * Buscar por ID de rollo de ingreso
     */
    Flux<DetailOrdenSalidaPesoEntity> findByIdRolloIngreso(Integer idRolloIngreso);
}