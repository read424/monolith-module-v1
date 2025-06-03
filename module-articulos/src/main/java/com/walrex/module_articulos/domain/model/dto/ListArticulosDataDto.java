package com.walrex.module_articulos.domain.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListArticulosDataDto {
    private List<ItemArticuloResponse> articulos_ids;
}
