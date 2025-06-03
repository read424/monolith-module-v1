package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("almacenes.detordeningresopeso")
public class DetalleRolloEntity {
    @Id
    @Column("id_detordeningresopeso")
    private Integer id;
    @Column("id_ordeningreso")
    private Integer ordenIngreso;
    @Column("cod_rollo")
    private String codRollo;
    @Column("peso_rollo")
    private BigDecimal pesoRollo;
    @Column("id_detordeningreso")
    private Integer idDetOrdenIngreso;
    @Column("id_rollo_ingreso")
    private Integer idRolloIngreso;
    private Integer status;
    @Column("peso_devolucion")
    private BigDecimal pesoDevolucion;
    private String observacion;
    private Integer num_cardinal;
    private OffsetDateTime create_at;
    private OffsetDateTime update_at;
}