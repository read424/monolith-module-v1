package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterGuideNoRollsDetail {
    private Integer id_articulo;
    private Integer nu_rollos;
    private Double peso_ref;
    private String lote;
}
