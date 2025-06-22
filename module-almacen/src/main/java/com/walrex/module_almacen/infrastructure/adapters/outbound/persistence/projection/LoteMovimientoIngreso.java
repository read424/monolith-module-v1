package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoteMovimientoIngreso {
    private Integer id_lote;
    private Integer id_ordeningreso;
    private Integer id_detordeningreso;
    private String cod_ingreso;
    private String no_motivo;
}
