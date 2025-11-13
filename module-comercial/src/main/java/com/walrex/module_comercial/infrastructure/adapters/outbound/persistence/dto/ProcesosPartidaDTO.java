package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcesosPartidaDTO {
    private Integer idPartidaMaquina;
    private Integer idRuta;
    private Integer idProceso;
    private String noProceso;
    private LocalDate fecRealInicio;
    private LocalDate fecRealFin;
    private Integer status;
    private Integer isMainProceso;
}
