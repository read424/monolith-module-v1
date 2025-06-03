package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Articulo {
    private Integer id;
    private String codigo;
    private String descripcion;
    private Integer idUnidad;
    private Integer idUnidadSalida;
    private String is_multiplo;
    private Integer valor_conv;
    private BigDecimal stock;
}
