package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobarSalidaResponseDTO {
    private Integer idOrdenSalida;
    private String codigoSalida;
    private String mensaje;
    private Integer productosAprobados;
    private Integer productosOmitidos;
    //private List<ProductoAprobadoDTO> detalleAprobacion;
    private OffsetDateTime fechaAprobacion;
}
