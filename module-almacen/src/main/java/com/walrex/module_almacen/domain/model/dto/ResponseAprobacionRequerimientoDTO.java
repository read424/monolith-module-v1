package com.walrex.module_almacen.domain.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class ResponseAprobacionRequerimientoDTO {
    private Integer idOrdenSalida;
    private String codigoSalida;
    private String message;
    private Boolean success;
    private Integer productosAprobados;
    private Integer productosIgnorados;
    private OffsetDateTime fechaAprobacion;
    private List<ProductoAprobadoDTO> detalleAprobacion;
}
