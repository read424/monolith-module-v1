package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticuloRequerimiento {
    private Integer idDetalleOrden;
    private Integer idArticulo;
    private String descArticulo;
    private String abrevUnidad;
    private Double cantidad;
    private Boolean selected;
    private Integer deleted;
    private Integer idUnidadConsumo;
    private Integer idUnidad;
    private Integer idUnidadOld;
}
