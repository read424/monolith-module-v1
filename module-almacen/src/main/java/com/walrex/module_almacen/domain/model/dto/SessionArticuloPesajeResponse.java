package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionArticuloPesajeResponse {
    private Integer id_detordeningreso;
    private Integer cantidad;
    private Double total_kg;
    private List<RolloPesadoDTO> details;
}
