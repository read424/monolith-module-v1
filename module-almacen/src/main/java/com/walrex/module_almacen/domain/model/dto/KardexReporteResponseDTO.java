package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KardexReporteResponseDTO {
    private boolean success;
    private String message;
    private List<KardexArticuloDTO> data;
}
