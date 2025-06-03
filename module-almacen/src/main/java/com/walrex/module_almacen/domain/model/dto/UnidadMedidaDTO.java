package com.walrex.module_almacen.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnidadMedidaDTO {
    @JsonProperty("id")
    private Integer value;
    private String text;
    private Integer id_medida_si;
}
