package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("almacenes.tbmotivos")
public class MotivoEntity {
    @Id
    private Long id_motivo;

    private String no_motivo;

    private Integer status;

    private String descripcion;
}
