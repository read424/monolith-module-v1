package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleIngresoDTO {
    private Long id;
    private Long idOrdenIngreso;
    private Integer idArticulo;
    private Integer idUnidad;
    private Double cantidad;
    private Double costoCompra;
    private String observacion;
}
