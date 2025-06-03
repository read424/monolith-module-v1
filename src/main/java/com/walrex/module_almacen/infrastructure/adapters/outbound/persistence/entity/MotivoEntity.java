package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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
