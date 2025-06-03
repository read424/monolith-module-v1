package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Almacen {
    private Integer idAlmacen;
    private Integer tipoAlmacen;
}
