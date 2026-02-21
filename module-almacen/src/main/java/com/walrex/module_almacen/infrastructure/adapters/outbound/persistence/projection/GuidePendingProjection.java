package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidePendingProjection {
    private Integer id_ordeningreso;
    private LocalDate fec_registro;
    private String nu_serie;
    private String nu_comprobante;
    private String razon_social;
    private Integer id_detordeningreso;
    private String lote;
    private Integer id_articulo;
    private String cod_articulo;
    private String desc_articulo;
    private Integer total_rollos;
    private Integer num_rollo;
    private Integer rolls_saved;
}
