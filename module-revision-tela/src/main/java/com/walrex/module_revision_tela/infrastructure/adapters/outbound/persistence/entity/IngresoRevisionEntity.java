package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla revision_crudo.ingreso_revision
 * Representa el header de una revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("revision_crudo.ingreso_revision")
public class IngresoRevisionEntity {

    @Id
    @Column("id_revision")
    private Integer idRevision;

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("id_cliente")
    private Integer idCliente;

    @Column("fec_ingreso")
    private LocalDate fecIngreso;

    @Column("nu_serie")
    private String nuSerie;

    @Column("nu_comprobante")
    private String nuComprobante;

    @Column("id_periodo")
    private Integer idPeriodo;

    @Column("id_usuario")
    private Integer idUsuario;

    @Column("create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column("update_at")
    @Builder.Default
    private LocalDateTime updateAt = LocalDateTime.now();
}
