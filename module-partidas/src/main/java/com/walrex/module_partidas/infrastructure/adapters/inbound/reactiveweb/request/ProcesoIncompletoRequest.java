package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoIncompletoRequest {

    @NotNull(message = "El id_proceso no puede ser nulo")
    @JsonProperty("id_proceso")
    private Integer idProceso;

    @JsonProperty("id_tipo_maquina")
    private Integer idTipoMaquina;

    @JsonProperty("is_servicio")
    private Integer isServicio;

    @JsonProperty("id_det_ruta")
    private Integer idDetRuta;

    @JsonProperty("is_mainproceso")
    private Integer isMainProcesos;

    @JsonProperty("is_declarable")
    private Integer isDeclarable;

    @JsonProperty("id_partida_maquina")
    private Integer idPartidaMaquina;

    @JsonProperty("fec_real_inicio")
    private Integer fecRealInicio;

    @JsonProperty("fec_real_fin")
    private Integer fecRealFin;
}
