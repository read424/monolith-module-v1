package com.walrex.module_comercial.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesosProduccionDTO {
    private Integer idPartidaMaquina;
    private Integer idProceso;
    private String noProceso;
    private Integer isMainProceso;
    private Integer isStarted;
    private Integer isFinish;
    private Integer status;
}
