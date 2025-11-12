package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ArticuloKardex {
    private Integer idArticulo;
    private String codArticulo;
    private Integer factorConversion;
    private Integer idUnidad;
    private Integer idUnidadSalida;
    private BigDecimal precioAVG;
    private BigDecimal mtoValorUnidad;
    private Map<Integer, LoteMovimientoKardex> detalles;
}
