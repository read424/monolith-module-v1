package com.walrex.module_revision_tela.application.ports.output;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailIngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailRolloRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RevisionInventarioProjection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface RevisionInventarioPort {

    /**
     * Obtiene los datos para procesar la revisión de inventario
     *
     * @return Flux con los datos de las órdenes a procesar
     */
    Flux<RevisionInventarioProjection> obtenerDatosRevision();

    /**
     * Guarda el header de la revisión
     *
     * @param entity Entidad a guardar
     * @return Mono con la entidad guardada
     */
    Mono<IngresoRevisionEntity> guardarIngresoRevision(IngresoRevisionEntity entity);

    /**
     * Guarda el detalle de ingreso
     *
     * @param entity Entidad a guardar
     * @return Mono con la entidad guardada
     */
    Mono<DetailIngresoRevisionEntity> guardarDetailIngresoRevision(DetailIngresoRevisionEntity entity);

    /**
     * Guarda el detalle de rollo
     *
     * @param entity Entidad a guardar
     * @return Mono con la entidad guardada
     */
    Mono<DetailRolloRevisionEntity> guardarDetailRolloRevision(DetailRolloRevisionEntity entity);
}
