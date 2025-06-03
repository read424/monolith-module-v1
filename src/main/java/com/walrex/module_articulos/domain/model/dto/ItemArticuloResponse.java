package com.walrex.module_articulos.domain.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemArticuloResponse {
    private Integer id_articulo;
    private String cod_articulo;
    private String desc_articulo;
}
