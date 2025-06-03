package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteDTO {
    private Integer idsalida_ote;
    private Integer id_lote;
    private Double cantidad;
    private Double precioUnitario;
    private Double totalMonto;
}