package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlmacenTipoIngresoLogisticaRequestDto {

    @NotBlank(message = "Nombre de almacen es obligatorio")
    private String text;

    @JsonProperty("value")
    @NotNull(message = "Id de almacen es obligatorio")
    private Integer id_almacen;

    @NotNull(message = "Tipo de almacen es obligatorio")
    private Integer id_tipo_almacen;
}
