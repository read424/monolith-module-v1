package com.walrex.module_partidas.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDeclaracionCalidadDTO {
    private String razonSocial;
    private LocalDate fecProgramacion;
    private LocalDate fecRealInicio;
    private LocalDate fechaDeclaracion;
    private LocalDate fecIngreso;
    private String codPartida;
    private String descArticulo;
    private String noColor;
    private Integer cntRollos;
    private String tipoDeclaracion;
    private Integer nivelCritico;
    private String descMotivoRechazo;
    private Integer isObservado;
    private String observacion;
}
