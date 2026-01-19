package com.walrex.module_revision_tela.domain.model;

import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Modelo de dominio para Periodo de Revisión
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table()
public class PeriodoRevision {

    private Integer idPeriodo;
    private String monthPeriodo;
    private Integer anioPeriodo;
    private String status;
}
