package com.walrex.module_revision_tela.domain.model.dto;

/**
 * Response DTO para el análisis de inventario y levantamiento
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public record AnalysisInventoryLiftingResponse(
    Integer idPeriodo,
    Integer totalLevantamientosProcesados,
    Integer totalRollosAsignados,
    String mensaje
) {
}
