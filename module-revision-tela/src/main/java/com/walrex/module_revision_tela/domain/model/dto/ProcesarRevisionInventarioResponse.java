package com.walrex.module_revision_tela.domain.model.dto;

/**
 * Response DTO para el proceso de revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public record ProcesarRevisionInventarioResponse(
    Integer idPeriodo,
    Integer totalOrdenesProcesadas,
    Integer totalDetallesCreados,
    Integer totalRollosCreados,
    String mensaje
) {
}
