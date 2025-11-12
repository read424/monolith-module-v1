package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity.OrdenProduccionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC reactivo para OrdenProduccionEntity.
 */
@Repository
public interface OrdenProduccionR2dbcRepository extends ReactiveCrudRepository<OrdenProduccionEntity, Integer> {

    /**
     * Busca una orden de producción por su código.
     *
     * @param codOrdenProduccion el código de la orden de producción
     * @return Mono con la orden de producción encontrada
     */
    Mono<OrdenProduccionEntity> findByCodOrdenProduccion(String codOrdenProduccion);

    /**
     * Busca órdenes de producción por estado.
     *
     * @param status el estado (1 = activo, 0 = inactivo)
     * @return Flux con las órdenes de producción filtradas por estado
     */
    Flux<OrdenProduccionEntity> findByStatus(Integer status);

    /**
     * Busca órdenes de producción por artículo.
     *
     * @param idArticulo el identificador del artículo
     * @return Flux con las órdenes de producción del artículo
     */
    Flux<OrdenProduccionEntity> findByIdArticulo(Integer idArticulo);
}