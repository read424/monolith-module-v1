package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoAprobadoDTO {
    private Integer idDetalleOrden;
    private Integer idArticulo;
    private String descripcionArticulo;
    private Double cantidad;
    private String unidad;
    private Boolean status;
    private String motivo;
}
