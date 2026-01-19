package com.walrex.module_revision_tela.domain.model;

import lombok.*;

/**
 * Modelo de dominio que representa una corrección de status necesaria
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusCorreccion {

    private Integer idOrdeningreso;
    private Integer statusActual;
    private Integer statusNuevo;
}
