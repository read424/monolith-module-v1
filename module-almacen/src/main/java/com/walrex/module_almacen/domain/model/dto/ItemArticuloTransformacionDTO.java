package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemArticuloTransformacionDTO {
    private Integer id_articulo;
    private String cod_articulo;
    private String desc_articulo;
    private String abrev_unidad;
    private Integer id_unidad;
    private Double cantidad;
    private Integer id_unidad_consumo;
}
