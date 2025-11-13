package com.walrex.module_comercial.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para guardar solicitud de cambio de servicio
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuardarSolicitudCambioRequestDTO {

    private Boolean aplicarOtrasPartidas;

    private Integer cntPartidas;

    private Integer idOrdenproduccion;

    private Integer idPartida;

    private Integer isDelivered;

    private Integer idUsuario;

    private SolicitudCambioDTO solicitudCambio;
}
