package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurvaDisenoResponse {
    private Integer id;
    private String descripcion;

    @JsonRawValue
    @JsonProperty("curva_diseno")
    private String curvaDiseno;

    private String version;

    @JsonProperty("id_laboratorista")
    private Integer idLaboratorista;

    private String laboratorista;

    @JsonProperty("id_supervisor")
    private Integer idSupervisor;

    private String supervisor;
    private Integer status;
    private Boolean locked;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
