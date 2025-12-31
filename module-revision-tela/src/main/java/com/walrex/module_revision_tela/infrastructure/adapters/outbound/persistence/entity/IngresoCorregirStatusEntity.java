package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla almacenes.ingreso_corregir_status
 * Almacena los cambios de status que deben aplicarse a las guías de ingreso
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenes.ingreso_corregir_status")
public class IngresoCorregirStatusEntity {

    @Id
    @Column("id_ingreso_corregir_status")
    private Integer idIngresoCorregirStatus;

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("status_actual")
    private Integer statusActual;

    @Column("status_nuevo")
    private Integer statusNuevo;

    @Column("fec_registro")
    @Builder.Default
    private OffsetDateTime fecRegistro = OffsetDateTime.now();

    @Column("procesado")
    @Builder.Default
    private Boolean procesado = false;
}
