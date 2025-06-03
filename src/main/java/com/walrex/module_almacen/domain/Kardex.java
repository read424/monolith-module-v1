package com.walrex.module_almacen.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class Kardex {
    Integer idKardex;
    Integer tipoKardex;
    String detalle;
    BigDecimal cantidad;
    BigDecimal valorUnidad;
    BigDecimal valorTotal;
    LocalDate fechaMovimiento;
    Integer idArticulo;
    Integer status;
    Integer idUnidad;
    Integer idUnidadSalida;
    Integer idAlmacen;
    BigDecimal saldoStock;
    Integer idDocumento;
    Integer idDetalleDocumento;
    Integer idLote;
    BigDecimal saldoLote;
}
