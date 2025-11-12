package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransformacionResponseDTO {
    private Long idOrdenIngreso;
    private String codigoIngreso;
    private Long idOrdenSalida;
    private String codigoSalida;
    private Boolean success;
}
