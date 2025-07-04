package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad para la trazabilidad de devoluciones de rollos
 * Tabla: almacenes.devolucion_rollos
 * Vincula rollos de ingreso con rollos de devolución
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.devolucion_rollos")
public class DevolucionRollosEntity {
    @Id
    @Column("id_devolucion_rollo")
    private Long id;

    @Column("id_detordensalidapeso")
    private Long idDetOrdenSalidaPeso;

    @Column("id_ordeningreso")
    private Integer idOrdenIngreso;

    @Column("id_detordeningreso")
    private Integer idDetOrdenIngreso;

    @Column("id_detordeningresopeso")
    private Integer idDetOrdenIngresoPeso;

    @Column("create_at")
    private OffsetDateTime createAt;

    @Column("update_at")
    private OffsetDateTime updateAt;
}