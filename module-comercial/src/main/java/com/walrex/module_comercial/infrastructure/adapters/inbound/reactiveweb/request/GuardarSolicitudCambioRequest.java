package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO de entrada HTTP para guardar solicitud de cambio de servicio.
 * Pertenece a la capa de infrastructure (adapter inbound).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuardarSolicitudCambioRequest {

    @NotNull(message = "Id Partida es obligatorio")
    @JsonProperty("id_partida")
    private Integer idPartida;

    @JsonProperty("aplicar_otras_partidas")
    private Boolean aplicarOtrasPartidas;

    @JsonProperty("cnt_partidas")
    private Integer cntPartidas;

    @JsonProperty("id_ordenproduccion")
    private Integer idOrdenproduccion;

    private Integer isDelivered;

    @JsonProperty("solicitud_cambio")
    private SolicitudCambioRequest solicitudCambio;
}
