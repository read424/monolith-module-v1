package com.walrex.module_almacen.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Motivo {
    private Integer idMotivo;
    private String descMotivo;
}
