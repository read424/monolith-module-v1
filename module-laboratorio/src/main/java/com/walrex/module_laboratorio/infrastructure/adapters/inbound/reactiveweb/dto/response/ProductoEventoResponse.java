package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEventoResponse {
    private Integer id;
    private String nombre;
    private Integer status;
}
