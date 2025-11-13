package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para datos de solicitud de cambio de servicio.
 * Pertenece a la capa de infrastructure (adapter inbound).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudCambioRequest {

    @JsonProperty("id_ordenproduccion")
    private Integer idOrdenproduccion;

    @JsonProperty("id_ruta")
    private Integer idRuta;

    private String codOrdenproduccion;

    @JsonProperty("desc_articulo")
    private String descArticulo;

    @JsonProperty("id_gama")
    private Integer idGama;

    @JsonProperty("id_precio")
    private Integer idPrecio;

    private Double precio;

    @JsonProperty("id_orden")
    private Integer idOrden;

    @JsonProperty("id_det_os")
    private Integer idDetOs;
}
