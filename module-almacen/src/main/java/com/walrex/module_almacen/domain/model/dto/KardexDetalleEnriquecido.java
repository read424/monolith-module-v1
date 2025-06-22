package com.walrex.module_almacen.domain.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class KardexDetalleEnriquecido {
    private Long idKardex;
    private Integer tipoKardex;
    private String detalle;
    private BigDecimal cantidad;
    private BigDecimal valorUnidad;
    private BigDecimal valorTotal;
    private LocalDate fechaMovimiento;
    private Integer idArticulo;
    private String descArticulo;
    private Integer status;
    private Integer idUnidad;
    private String abrevUnidad;
    private String descUnidad;
    private Integer idUnidadSalida;
    private String abrevSalida;
    private String descUnidadSalida;
    private Integer idAlmacen;
    private BigDecimal saldoStock;
    private BigDecimal saldoLote;
    private Integer idLote;
    private Integer idDocumento;
    private Integer idDetalleDocumento;
    private String codigoDocumento;
    private String descDocumentoIngreso;
}
