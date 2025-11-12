package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrdenEgresoResponse {
    @JsonProperty("affected_rows")
    private Integer affectedRows;
    private Boolean success;
    private String message;
}
