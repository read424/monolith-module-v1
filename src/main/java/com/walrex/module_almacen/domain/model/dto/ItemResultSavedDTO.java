package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemResultSavedDTO {
    private Integer id;
    private Integer id_articulo;
    private Integer id_lote;
    private Integer id_unidad;
    private Double cantidad;
    private Double precio;
    private String observacion;
}
