package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemProductDTO {
    private Long id_orden;
    private Long id_detail;
    private Integer id_articulo;
    private Integer id_unidad;
    private Double cantidad;
    private Double precio;
    private Double tot_amount;
    private String observacion;
    private List<LoteDTO> lotes;
}
