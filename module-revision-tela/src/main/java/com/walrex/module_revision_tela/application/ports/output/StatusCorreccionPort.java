package com.walrex.module_revision_tela.application.ports.output;

import com.walrex.module_revision_tela.domain.model.StatusCorreccion;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistir correcciones de status
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface StatusCorreccionPort {

    /**
     * Guarda una corrección de status en la tabla ingreso_corregir_status
     *
     * @param correccion La corrección a guardar
     * @return Mono con la corrección guardada
     */
    Mono<StatusCorreccion> guardarCorreccion(StatusCorreccion correccion);
}
