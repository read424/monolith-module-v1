package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad para el detalle de peso de rollos en órdenes de salida
 * Tabla: almacenes.detordensalidapeso
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table("almacenes.detordensalidapeso")
public class DetailOrdenSalidaPesoEntity {
    @Id
    @Column("id_detordensalidapeso")
    private Long id;

    @Column("id_ordensalida")
    private Long idOrdenSalida;

    @Column("id_detalle_orden")
    private Long idDetalleOrden;

    @Column("cod_rollo")
    private String codRollo;

    @Column("peso_rollo")
    private BigDecimal pesoRollo;

    @Column("id_det_partida")
    private Integer idDetPartida;

    @Column("id_rollo_ingreso")
    private Integer idRolloIngreso;

    private Integer status;

    @Column("create_at")
    private OffsetDateTime createAt;

    @Column("update_at")
    private OffsetDateTime updateAt;
}