package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurvaDisenoCreateRequest {
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotBlank(message = "La versión es obligatoria")
    private String version;

    @NotNull(message = "json es obligatorio")
    @JsonProperty("json")
    private JsonNode curvaDiseno;
}
