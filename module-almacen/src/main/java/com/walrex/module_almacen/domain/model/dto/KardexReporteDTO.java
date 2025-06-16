package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class KardexReporteDTO {
    private Integer idArticulo;
    private String nombreArticulo;
    private BigDecimal stockInicial;
    private BigDecimal totalIngresos;
    private BigDecimal totalSalidas;
    private BigDecimal stockFinal;
    private String advertencia;
}
