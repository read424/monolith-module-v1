package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnidadMedidaRequest {
    @JsonProperty("id")
    @NotNull(message="Campo value de unidad es obligatorio")
    private Integer value;
    @JsonProperty("descripcion")
    @NotBlank(message = "Campo text de unidad es obligatorio")
    private String text;
    @JsonProperty("idMedidaSi")
    private Integer id_medida_si;
}
