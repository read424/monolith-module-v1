package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolloPesadoDTO {
    private Integer id_detordeningresopeso;
    private String cod_rollo;
    private Double peso;
}
