package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_ecomprobantes.domain.model.entity.TipoSerieEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository reactivo para la entidad TipoSerieEntity.
 * 
 * Proporciona métodos para acceder a la tabla tipo_serie del esquema
 * facturacion.
 */
@Repository
public interface TipoSerieRepository extends ReactiveCrudRepository<TipoSerieEntity, Integer> {

    /**
     * Busca series por tipo de comprobante
     * 
     * @param idCompro Identificador del tipo de comprobante
     * @return Flux con las series encontradas
     */
    Flux<TipoSerieEntity> findByIdCompro(Integer idCompro);

    /**
     * Busca series activas por tipo de comprobante
     * 
     * @param idCompro Identificador del tipo de comprobante
     * @return Flux con las series activas encontradas
     */
    Flux<TipoSerieEntity> findByIdComproAndIlEstadoTrue(Integer idCompro);

    /**
     * Busca una serie específica por número de serie
     * 
     * @param nuSerie Número de serie
     * @return Mono con la serie encontrada o vacío si no existe
     */
    Mono<TipoSerieEntity> findByNuSerie(String nuSerie);

    /**
     * Busca series que son comprobantes electrónicos (CPE)
     * 
     * @return Flux con las series CPE
     */
    Flux<TipoSerieEntity> findByIsCpe(Integer isCpe);

    /**
     * Busca series activas que son comprobantes electrónicos
     * 
     * @return Flux con las series CPE activas
     */
    Flux<TipoSerieEntity> findByIsCpeAndIlEstadoTrue(Integer isCpe);
}