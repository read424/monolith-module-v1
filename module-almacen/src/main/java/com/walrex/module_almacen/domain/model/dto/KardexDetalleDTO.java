package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KardexDetalleDTO {
    private Integer idOrdenDocumento;
    private Integer typeKardex;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioCompra;
    private BigDecimal totalCompra;
    private BigDecimal stockActual;
    private BigDecimal stockLote;
    private Integer idUnidad;
    private String descUnidad;
    private Integer idUnidadSalida;
    private String descUnidadSalida;
    private LocalDate fecMovimiento;
}
