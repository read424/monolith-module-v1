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
public class TransformacionProcesoResponseDTO {
    private Long id;
    private String codIngreso;
    private Long idOrdensalida;
    private String codEgreso;
    private ArticuloTransformacionDTO articuloProducido;
    private Double cantidadProducida;
    private List<DetalleEgresoDTO> insumosConsumidos;
}
