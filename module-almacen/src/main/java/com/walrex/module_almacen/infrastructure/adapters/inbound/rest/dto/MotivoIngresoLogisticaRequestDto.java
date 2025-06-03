package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MotivoIngresoLogisticaRequestDto {
    @NotNull(message = "Id motivo es obligatorio")
    @JsonProperty("value")
    private Integer id;

    @NotBlank(message = "Descripcion motivo es obligatorio")
    @JsonProperty("text")
    private String descripcion;

    @JsonProperty("isOc")
    private Integer isOrdenCompra;
}