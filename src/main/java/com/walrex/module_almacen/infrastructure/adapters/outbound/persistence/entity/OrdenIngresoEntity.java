package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("almacenes.ordeningreso")
public class OrdenIngresoEntity {
    @Id
    @Column("id_ordeningreso")
    private Long id;
    private Integer id_cliente;
    private Integer id_motivo;
    private Integer id_origen;
    private Integer id_comprobante;
    private String nu_comprobante;
    private String observacion;
    private LocalDate fec_ingreso;
    @Column("fec_ref")
    private LocalDate fec_referencia;
    @Column("fec_registro")
    private OffsetDateTime create_at;
    private Integer status;
    private String nu_serie;
    private String cod_ingreso;
    private Integer id_almacen;
    private Integer id_orden;
    private String descripcion;
    private String comprobante_ref;
    private Integer condicion;
    private OffsetDateTime update_at;
    private Integer idOrdenServ;
}
