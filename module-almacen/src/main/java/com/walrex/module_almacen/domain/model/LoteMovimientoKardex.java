package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoteMovimientoKardex {
    private Long firstIdKardex;
    private Integer idLote;
    private BigDecimal cantidad;
    private BigDecimal cantidadStock;
    private BigDecimal cantidadLote;
    private BigDecimal precioVenta;
    private BigDecimal totalValorizado;
}
