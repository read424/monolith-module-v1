package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ItemKardexDTO {
    private Integer typeKardex;
    private LocalDate fechaMovimiento;
    private String descripcion;
    private Integer idArticulo;
    private Integer idUnidad;
    private BigDecimal cantidad;
    private BigDecimal valorUnidad;
    private BigDecimal valorTotal;
    private Integer idUnidadSalida;
    private Integer idAlmacen;
    private BigDecimal saldoStock;
    private Integer idLote;
    private BigDecimal saldoLote;
    private Integer idDocumento;
    private Integer idDetalleDocumento;
}
