package com.walrex.module_articulos.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticuloSearchCriteria {
    private String search;
    private int page;
    private int size;
    private Integer idTipoProducto;
}
