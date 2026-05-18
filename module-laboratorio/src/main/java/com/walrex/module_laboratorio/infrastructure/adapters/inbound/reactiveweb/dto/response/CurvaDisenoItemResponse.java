package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurvaDisenoItemResponse {
    private Integer id;
    @JsonProperty("id_curva_diseno")
    private Integer idCurvaDiseno;
    @JsonRawValue
    private String curva;
}
