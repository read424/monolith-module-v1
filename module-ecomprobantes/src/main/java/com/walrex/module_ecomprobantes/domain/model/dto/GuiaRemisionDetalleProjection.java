package com.walrex.module_ecomprobantes.domain.model.dto;

import lombok.*;

/**
 * Proyección para los detalles de guía de remisión.
 * Contiene los datos de cada línea del comprobante.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaRemisionDetalleProjection {

    // Datos del detalle
    private Integer idDetComprobante;
    private Integer idProducto;
    private String codArticulo;
    private String descArticulo;
    private String codUnidad;
    private Double peso;
}