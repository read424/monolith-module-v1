package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecetaCurvaDisenoResponse {
    private Integer id;
    @JsonRawValue
    private String curvaDiseno;
}
