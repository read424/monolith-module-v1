package com.walrex.module_comercial.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenProductionPartidaResponseDTO {
    private Integer idOrdenProduccion;
    private String codOrdenProduccion;
    private String descArticulo;
    private Integer idOrden;
    private Integer idDetOS;
    private Integer idRuta;
    private Integer idOrdenIngreso;
    private Integer cntPartidas;
    private Integer isDelivered;
    private Boolean isMainProductionSuccess;
    private ProcesosProduccionDTO[] procesos;
}
