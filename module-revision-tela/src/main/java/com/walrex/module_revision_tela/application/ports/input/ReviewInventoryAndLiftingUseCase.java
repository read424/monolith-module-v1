package com.walrex.module_revision_tela.application.ports.input;

import com.walrex.module_revision_tela.domain.model.dto.AnalysisInventoryLiftingResponse;
import reactor.core.publisher.Mono;

public interface ReviewInventoryAndLiftingUseCase {

    /**
     * Ejecuta el análisis de inventario levantado para un periodo específico,
     * cruzando los datos del levantamiento con los rollos inventariados
     * y asignando el id_levantamiento correspondiente a cada rollo
     *
     * @param idPeriodo ID del periodo de revisión
     * @return Mono con las estadísticas del análisis (levantamientos procesados y rollos asignados)
     */
    Mono<AnalysisInventoryLiftingResponse> executeAnalysis(Integer idPeriodo);

}
