package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseCreateOrdenIngresoLogisticaDto {
    private Boolean success;
    private Integer affected_rows;
    private String message;
}
