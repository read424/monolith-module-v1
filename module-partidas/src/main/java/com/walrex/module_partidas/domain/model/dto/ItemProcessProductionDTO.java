package com.walrex.module_partidas.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemProcessProductionDTO {

    private Integer idPartidaMaquina;

    private Integer idDetRuta;

    private Integer idProceso;

    private Integer idTipoMaquina;

    private Integer idMaquina;
}
