package com.walrex.module_articulos.infrastructure.adapters.inbound.reactiveweb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestSearchDTO {
    private String query;
    private int page;
    private int size;
    private Integer idTipoProducto;
}
