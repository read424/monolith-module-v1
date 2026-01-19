package com.walrex.module_revision_tela.domain.model.dto;

import java.util.List;

/**
 * Response DTO para el resultado del proceso de corrección de status
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public record FixedStatusGuieResponse(
    Integer totalGuiasAnalizadas,
    Integer totalCorreccionesNecesarias,
    List<StatusCorreccionDTO> correcciones,
    String mensaje
) {
}
