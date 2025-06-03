package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DetalleRollo {
    private Integer id;
    private String codRollo;
    private BigDecimal pesoRollo;
    private Integer ordenIngreso;
    private Integer idDetOrdenIngreso;
    private Integer delete;
}
