package com.walrex.module_almacen.domain.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemIngreso {
    private Integer id_detordeningreso;
    private Integer id_ordeingreso;
    private Integer id_articulo;
    private Integer id_unidad;
    private String lote;
    private Double nu_rollos;
    private String observacion;
    private Double costo_compra;
    private Integer status;
    private Integer id_moneda;

}
