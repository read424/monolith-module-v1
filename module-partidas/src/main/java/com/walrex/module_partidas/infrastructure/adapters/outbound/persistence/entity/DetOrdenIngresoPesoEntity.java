package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.*;

/**
 * Entidad que mapea la tabla almacenes.detordeningresopeso
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenes.detordeningresopeso")
public class DetOrdenIngresoPesoEntity {

    @Id
    @Column("id_detordeningresopeso")
    private Integer idDetordeningresopeso;

    @Column("id_ordeningreso")
    private Integer idOrdeningreso;

    @Column("cod_rollo")
    private String codRollo;

    @Column("peso_rollo")
    private BigDecimal pesoRollo;

    @Column("id_detordeningreso")
    private Integer idDetordeningreso;

    @Column("id_rollo_ingreso")
    private Integer idRolloIngreso;

    @Column("status")
    private Integer status;

    @Column("peso_devolucion")
    private BigDecimal pesoDevolucion;

    @Column("id_det_peso_liquidacion")
    private Integer idDetPesoLiquidacion;

    @Column("devolucion")
    private Integer devolucion;

    @Column("observacion")
    private String observacion;

    @Column("create_at")
    private OffsetDateTime createAt;

    @Column("update_at")
    private OffsetDateTime updateAt;

    @Column("peso_merma")
    private BigDecimal pesoMerma;

    @Column("peso_acabado")
    private BigDecimal pesoAcabado;

    @Column("peso_percha")
    private BigDecimal pesoPercha;

    @Column("num_cardinal")
    private Integer numCardinal;
}
