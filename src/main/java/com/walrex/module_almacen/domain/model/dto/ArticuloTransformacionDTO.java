package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticuloTransformacionDTO {
    private Integer idArticulo;
    private String codArticulo;
    private String descArticulo;
    private Integer idUnidad;
    private Integer idUnidadSalida;
}
