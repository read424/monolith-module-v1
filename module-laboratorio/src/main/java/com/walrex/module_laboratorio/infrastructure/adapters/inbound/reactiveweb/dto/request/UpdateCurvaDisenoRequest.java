package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCurvaDisenoRequest {
    @NotNull(message = "curvaDiseno es obligatorio")
    private JsonNode curvaDiseno;
}
