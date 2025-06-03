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
@Table("almacenes.tb_ingreso_rollo_devoluciones")
public class DetailRolloDevolucionEntity {
    @Id
    @Column("id_rollo_devolucion")
    private Long id;
    private Integer id_detalle_devolucion;
    private Integer id_ordeningreso;
    private Integer id_det_peso_liquidacion;
    private String cod_rollo;
    private Double peso;
    private OffsetDateTime create_at;
    private OffsetDateTime update_at;
    private String observacion;
    private Integer id_motivo_rechazo;
}
