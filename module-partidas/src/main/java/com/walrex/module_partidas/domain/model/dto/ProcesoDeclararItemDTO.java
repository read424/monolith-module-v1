package com.walrex.module_partidas.domain.model.dto;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProcesoDeclararItemDTO {

    private Integer idPartidaMaquina;

    private Integer idProceso;

    private Integer idTipoMaquina;

    private Integer isServicio;

    private Integer idDetRuta;

    private Integer isMainProcesos;

    private Integer isDeclarable;

    private Integer fecRealInicio;

    private Integer fecRealFin;
}
