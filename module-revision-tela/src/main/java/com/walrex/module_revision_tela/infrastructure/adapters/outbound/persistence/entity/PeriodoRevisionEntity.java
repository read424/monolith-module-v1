package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla revision_crudo.periodo_revision
 * Representa un periodo de revisión con mes y año
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("revision_crudo.periodo_revision")
public class PeriodoRevisionEntity {

    @Id
    @Column("id_periodo")
    private Integer idPeriodo;

    @Column("month_periodo")
    private String monthPeriodo;

    @Column("anio_periodo")
    private Integer anioPeriodo;

    @Column("status")
    private String status;

    @Column("create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column("update_at")
    @Builder.Default
    private LocalDateTime updateAt = LocalDateTime.now();
}
