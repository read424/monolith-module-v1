package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table("logistica.tbarticulos")
public class ArticuloEntity {
    @Id()
    @Column("id_articulo")
    private Long id;
    private Integer id_familia;
    private Integer id_grupo;
    private String cod_articulo;
    private String desc_articulo;
    private Integer id_medida;
    private Integer id_unidad;
    private Integer id_marca;
    private String descripcion;
    private Double mto_compra;
    @Column("fec_ingreso")
    private LocalDate create_at;
    private Integer status;
    private Integer id_unidad_consumo;
    private Integer id_moneda;
    private Boolean is_transformacion;
}
