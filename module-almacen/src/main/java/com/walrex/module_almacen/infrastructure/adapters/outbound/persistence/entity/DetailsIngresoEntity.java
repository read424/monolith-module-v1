package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("almacenes.detordeningreso")
public class DetailsIngresoEntity {
    @Id()
    @Column("id_detordeningreso")
    private Long id;
    private Long id_ordeningreso;
    private Integer id_articulo;
    private Integer id_unidad;
    private String lote;
    private Double peso_ref;
    @Column("peso_alm")
    private Double peso_ingreso;
    private Double peso_dif;
    @Column("nu_rollos")
    private Double cantidad;
    private String observacion;
    private Double costo_compra;
    private Long status;
    private Long id_kardex;
    private Integer id_moneda;
    private Integer excento_imp;
}
