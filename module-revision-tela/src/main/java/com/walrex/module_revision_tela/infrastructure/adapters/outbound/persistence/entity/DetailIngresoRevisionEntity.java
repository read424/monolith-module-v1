package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla revision_crudo.detail_ingreso_revision
 * Representa el detalle de artículos en una revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("revision_crudo.detail_ingreso_revision")
public class DetailIngresoRevisionEntity {

    @Id
    @Column("id_detail")
    private Integer idDetail;

    @Column("id_revision")
    private Integer idRevision;

    @Column("id_detordeningreso")
    private Integer idDetordeningreso;

    @Column("id_articulo")
    private Integer idArticulo;
}
