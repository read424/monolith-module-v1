package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultAjustIngresoDTO {
    private Integer id;
    private Integer num_saved;
    private List<ItemResultSavedDTO> details;
}
