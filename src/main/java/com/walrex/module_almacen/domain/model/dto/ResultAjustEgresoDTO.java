package com.walrex.module_almacen.domain.model.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultAjustEgresoDTO {
    private Integer id;
    private Integer num_saved;
    private List<ItemArticuloEgreso> details;
}
