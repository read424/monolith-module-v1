package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla revision_crudo.detail_rollo_revision
 * Representa el detalle de rollos en una revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("revision_crudo.detail_rollo_revision")
public class DetailRolloRevisionEntity {

    @Id
    @Column("id")
    private Integer id;

    @Column("id_detail")
    private Integer idDetail;

    @Column("id_detordeningreso")
    private Integer idDetordeningreso;

    @Column("id_detordeningresopeso")
    private Integer idDetordeningresopeso;

    @Column("id_partida")
    private Integer idPartida;

    @Column("id_det_partida")
    private Integer idDetPartida;

    @Column("status")
    @Builder.Default
    private Integer status = 1;

    @Column("create_at")
    @Builder.Default
    private LocalDateTime createAt = LocalDateTime.now();

    @Column("update_at")
    @Builder.Default
    private LocalDateTime updateAt = LocalDateTime.now();

    @Column("date_out")
    private LocalDateTime dateOut;

    @Column("as_crudo")
    @Builder.Default
    private Integer asCrudo = 0;

    @Column("as_reingreso")
    @Builder.Default
    private String asReingreso = "0";

    @Column("id_levantamiento")
    private Integer idLevantamiento;

    @Column("id_detordeningresopeso_alm")
    private Integer idDetordeningresopesoAlm;

    @Column("status_almacen")
    private Integer statusAlmacen;

    @Column("status_roll_ing")
    private Integer statusRollIng;
}
