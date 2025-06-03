package com.walrex.module_almacen.domain.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSalidaDto {
    private Long id_ordensalida;
    private Long id_detalle_orden;
    private Integer id_articulo;
    private Integer id_unidad;
    private Double cantidad;
    private Double precio;
    private Double tot_monto;
}
