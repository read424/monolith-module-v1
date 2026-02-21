package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenes.session_pesaje_activa")
public class SessionPesajeActivaEntity {
    @Id
    private Integer id;

    @Column("id_detordeningreso")
    private Integer idDetOrdenIngreso;

    @Column("cnt_rollos")
    private Integer cntRollos;

    @Column("tot_kg")
    private Double totKg;

    @Column("cnt_registro")
    private Integer cntRegistro;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("upated_at")
    private OffsetDateTime updatedAt;

    private String status;
}
