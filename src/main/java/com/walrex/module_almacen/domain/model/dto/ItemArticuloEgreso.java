package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemArticuloEgreso {
    private Integer id_detalle_orden;
    private Integer id_articulo;
    private Integer id_unidad;
    private Double cantidad;
    private List<LoteDTO> a_lote;
}
