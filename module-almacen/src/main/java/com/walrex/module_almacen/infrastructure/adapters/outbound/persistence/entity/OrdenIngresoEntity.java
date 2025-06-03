package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("almacenes.ordeningreso")
public class OrdenIngresoEntity {
    @Id()
    private Long id_ordeningreso;
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
    private String descripcion;
    private String comprobante_ref;
    private Integer condicion;
    private OffsetDateTime update_at;
    private Integer idOrdenServ;
}
