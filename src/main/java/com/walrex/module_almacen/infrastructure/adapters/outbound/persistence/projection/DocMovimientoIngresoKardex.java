package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DocMovimientoIngresoKardex {
    private Integer id_lote;
    private Integer id_ordeningreso;
    private String cod_ingreso;
    private LocalDate fec_ingreso;
    private String no_motivo;
}
