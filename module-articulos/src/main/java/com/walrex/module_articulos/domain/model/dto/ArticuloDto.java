package com.walrex.module_articulos.domain.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ArticuloDto {
    private Long id_articulo;
    private Integer id_familia;
    private Integer id_grupo;
    private String cod_articulo;
    private String desc_articulo;
    private Integer id_medida;
    private Integer id_unidad;
    private Integer id_marca;
    private String descripcion;
    private Double mto_compra;
    private LocalDate fec_ingreso;
    private Integer status;
    private Integer id_unidad_consumo;
    private Integer id_moneda;
    private Boolean is_transformacion;
}
