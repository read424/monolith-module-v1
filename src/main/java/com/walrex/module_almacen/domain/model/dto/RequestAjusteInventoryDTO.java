package com.walrex.module_almacen.domain.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestAjusteInventoryDTO {
    private Integer id_motivo;
    private Integer id_almacen;
    private LocalDate fec_actualizacion;
    private List<ItemProductDTO> ingresos;
    private List<DetalleEgresoDTO> egresos;
}
