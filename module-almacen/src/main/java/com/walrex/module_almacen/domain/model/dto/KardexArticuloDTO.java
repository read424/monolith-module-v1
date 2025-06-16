package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KardexArticuloDTO {
    private Integer idArticulo;
    private String descArticulo;
    private BigDecimal precioAvg;//seria
    private BigDecimal totalValorizado;
    private BigDecimal stockDisponible;
    private List<KardexDetalleDTO> detalles;
}
