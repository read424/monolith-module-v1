package com.walrex.module_comercial.application.ports.output;

import com.walrex.module_comercial.domain.model.OrdenProduccion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de persistencia de OrdenProduccion.
 */
public interface OrdenProduccionOutputPort {

    /**
     * Guarda una orden de producción en el repositorio.
     *
     * @param ordenProduccion la orden de producción a guardar
     * @return Mono con la orden de producción guardada
     */
    Mono<OrdenProduccion> save(OrdenProduccion ordenProduccion);

    /**
     * Busca una orden de producción por su ID.
     *
     * @param id el identificador de la orden de producción
     * @return Mono con la orden de producción encontrada o vacío
     */
    Mono<OrdenProduccion> findById(Integer id);

    /**
     * Busca una orden de producción por su código.
     *
     * @param codigo el código de la orden de producción
     * @return Mono con la orden de producción encontrada o vacío
     */
    Mono<OrdenProduccion> findByCodigo(String codigo);

    /**
     * Obtiene todas las órdenes de producción.
     *
     * @return Flux con todas las órdenes de producción
     */
    Flux<OrdenProduccion> findAll();

    /**
     * Obtiene todas las órdenes de producción activas (status = 1).
     *
     * @return Flux con todas las órdenes de producción activas
     */
    Flux<OrdenProduccion> findAllActive();

    /**
     * Obtiene órdenes de producción por artículo.
     *
     * @param idArticulo el identificador del artículo
     * @return Flux con las órdenes de producción del artículo
     */
    Flux<OrdenProduccion> findByArticulo(Integer idArticulo);

    /**
     * Actualiza una orden de producción existente.
     *
     * @param ordenProduccion la orden de producción a actualizar
     * @return Mono con la orden de producción actualizada
     */
    Mono<OrdenProduccion> update(OrdenProduccion ordenProduccion);

    /**
     * Elimina una orden de producción por su ID.
     *
     * @param id el identificador de la orden de producción
     * @return Mono<Void> indicando la completitud
     */
    Mono<Void> deleteById(Integer id);
}
