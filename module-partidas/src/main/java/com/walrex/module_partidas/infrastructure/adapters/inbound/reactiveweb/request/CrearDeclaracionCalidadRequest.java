package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearDeclaracionCalidadRequest {

    @JsonProperty("id_ubicacion")
    private Integer idUbicacion;

    @JsonProperty("fecha_declaracion")
    private LocalDate fechaDeclaracion;

    @JsonProperty("id_partida")
    private Integer idPartida;

    @JsonProperty("id_maquina")
    private Integer idMaquina;

    @JsonProperty("id_auditor")
    private Integer idAuditor;

    @JsonProperty("nivel_critico")
    private Integer nivelCritico;

    @JsonProperty("id_motivo_rechazo")
    private Integer idMotivoRechazo;

    @JsonProperty("is_observado")
    private Integer isObservado;

    private String observacion;

    @JsonProperty("cnt_rollos")
    private Integer cntRollos;

    private Integer status;
}
