package com.walrex.module_revision_tela.application.ports.input;

import com.walrex.module_revision_tela.domain.model.dto.ProcesarRevisionInventarioResponse;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (Use Case) para procesar revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface ProcesarRevisionInventarioUseCase {

    /**
     * Procesa la revisión de inventario para órdenes con status_nuevo=10
     *
     * @param idUsuario ID del usuario que ejecuta el proceso
     * @return Mono con el resultado del procesamiento
     */
    Mono<ProcesarRevisionInventarioResponse> procesarRevision(Integer idUsuario);
}
