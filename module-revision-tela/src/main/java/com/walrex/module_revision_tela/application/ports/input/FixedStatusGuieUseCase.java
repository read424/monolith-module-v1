package com.walrex.module_revision_tela.application.ports.input;

import com.walrex.module_revision_tela.domain.model.dto.FixedStatusGuieResponse;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (Use Case) para regularizar el status de las guías
 * según los status de los rollos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface FixedStatusGuieUseCase {

    /**
     * Ejecuta el proceso de regularización de status de guías
     * Analiza los status de los rollos y determina qué guías necesitan actualización
     *
     * @return Mono con el resultado del proceso de corrección
     */
    Mono<FixedStatusGuieResponse> ejecutarCorreccionStatus();
}
