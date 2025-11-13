package com.walrex.module_comercial.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de cambio de servicio
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudCambioDTO {

    private Integer idOrdenproduccion;

    private Integer idRuta;

    private String codOrdenproduccion;

    private String descArticulo;

    private Integer idGama;

    private Integer idPrecio;

    private Double precio;

    private Integer idOrden;

    private Integer idDetOs;
}
