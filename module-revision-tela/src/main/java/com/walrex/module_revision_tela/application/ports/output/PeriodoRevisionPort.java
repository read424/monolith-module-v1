package com.walrex.module_revision_tela.application.ports.output;

import com.walrex.module_revision_tela.domain.model.PeriodoRevision;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para consultar periodos de revisión
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface PeriodoRevisionPort {

    /**
     * Obtiene el periodo activo (status=1)
     *
     * @return Mono con el periodo activo o empty si no existe
     */
    Mono<PeriodoRevision> obtenerPeriodoActivo();
}
