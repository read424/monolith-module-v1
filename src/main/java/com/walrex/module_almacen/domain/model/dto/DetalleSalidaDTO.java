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
public class DetalleSalidaDTO {
    private Long id;
    private Long idOrdenSalida;
    private Integer idArticulo;
    private Integer idUnidad;
    private Double cantidad;
    private Double precio;
    private Double totalMonto;
    private String observacion;
    private List<LoteDTO> lotes;
}
