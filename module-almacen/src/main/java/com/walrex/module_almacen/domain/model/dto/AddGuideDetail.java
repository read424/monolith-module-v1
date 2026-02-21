package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGuideDetail {
    private Integer id_articulo;
    private Integer cnt_rollos;
    private Double total_kg;
}
