package com.walrex.module_revision_tela.domain.model.dto;

/**
 * DTO para representar una corrección de status individual
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public record StatusCorreccionDTO(
    Integer idOrdeningreso,
    Integer statusActual,
    Integer statusNuevo
) {
}
