package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table("almacenes.tb_ingreso_liquidacion_devoluciones")
public class DetailLiquidacionDevolucionEntity {
    @Id
    @Column("id_detalle_devolucion")
    private Long id;
    private Integer id_ordeningreso;
    private Integer id_liquidacion;
    private Double peso_guia;
    private Integer id_usuario;
    private OffsetDateTime create_at;
    private OffsetDateTime update_at;
    private String observacion;
    private Integer id_motivo_rechazo;
    private Integer id_detordeningreso;
}
