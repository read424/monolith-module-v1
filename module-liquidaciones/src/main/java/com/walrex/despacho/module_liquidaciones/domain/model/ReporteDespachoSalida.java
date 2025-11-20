package com.walrex.despacho.module_liquidaciones.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReporteDespachoSalida(
    Long idLiquidacion,
    String razon_social,
    LocalDate fecLiquidacion,
    Short entregado,
    String codPartida,
    Integer cntRollos,
    String numGuia,
    BigDecimal kgIngreso,
    BigDecimal kgSalida,
    BigDecimal porcMerma
) {}
