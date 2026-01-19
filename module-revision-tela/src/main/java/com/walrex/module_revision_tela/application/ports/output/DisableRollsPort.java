package com.walrex.module_revision_tela.application.ports.output;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.UnliftedRollProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de deshabilitación de rollos
 */
public interface DisableRollsPort {

    /**
     * Obtiene los rollos sin levantamiento asignado para un periodo
     * @param idPeriodo ID del periodo
     * @return Flux con los rollos sin levantamiento
     */
    Flux<UnliftedRollProjection> getUnliftedRolls(Integer idPeriodo);

    /**
     * Actualiza el status de un rollo a deshabilitado (status = 10)
     * @param idDetOrdenIngresoPeso ID del rollo a deshabilitar
     * @return Mono con el número de filas actualizadas
     */
    Mono<Integer> disableRollStatus(Integer idDetOrdenIngresoPeso);
}
