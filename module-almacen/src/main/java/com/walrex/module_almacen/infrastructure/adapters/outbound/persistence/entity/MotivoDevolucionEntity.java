package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad para motivos específicos de devolución
 * Tabla: almacenes.tbmotivos_devoluciones
 * Almacena los motivos específicos para devoluciones de mercancía
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.tbmotivos_devoluciones")
public class MotivoDevolucionEntity {
    
    @Id
    private Long id;

    private String descripcion;

    private Integer status;

    @Column("create_at")
    private OffsetDateTime createAt;

    @Column("update_at")
    private OffsetDateTime updateAt;
} 