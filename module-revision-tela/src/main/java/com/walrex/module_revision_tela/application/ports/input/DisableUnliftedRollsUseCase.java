package com.walrex.module_revision_tela.application.ports.input;

import com.walrex.module_revision_tela.domain.model.dto.DisableRollsResponse;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para deshabilitar rollos que no fueron levantados en un periodo.
 * Actualiza el status a 10 (deshabilitado) tanto en guía de ingreso como en almacén.
 */
public interface DisableUnliftedRollsUseCase {

    /**
     * Deshabilita los rollos sin levantamiento asignado para un periodo específico
     * @param idPeriodo ID del periodo a procesar
     * @return Mono con las estadísticas del procesamiento
     */
    Mono<DisableRollsResponse> disableUnliftedRolls(Integer idPeriodo);
}
