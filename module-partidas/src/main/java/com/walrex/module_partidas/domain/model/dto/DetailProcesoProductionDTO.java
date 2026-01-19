package com.walrex.module_partidas.domain.model.dto;

import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DetailProcesoProductionDTO {

    private Integer idPartidaMaquina;

    private Integer idProceso;

    private Integer idDetRuta;

    private Integer idTipoMaquina;

    private Integer idMaquina;

    private Integer isServicio;

    private LocalDate fecRealInicio;

    private String horaInicio;

    private LocalDate fecRealFin;

    private String horaFin;

    private Integer idNivelObservacion;

    private Integer isMainProceso;

    private Integer status;

    private String observacion;
}
