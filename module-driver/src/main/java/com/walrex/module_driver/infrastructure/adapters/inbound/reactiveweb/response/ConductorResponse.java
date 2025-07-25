package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * DTO de respuesta para los datos del conductor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConductorResponse {

    @JsonProperty("id_conductor")
    private Long idConductor;

    @JsonProperty("tipo_documento")
    private TipoDocumentoResponse tipoDocumento;

    @JsonProperty("num_documento")
    private String numeroDocumento;

    @JsonProperty("apellidos")
    private String apellidos;

    @JsonProperty("nombres")
    private String nombres;

    @JsonProperty("num_licencia")
    private String licencia;

    /**
     * DTO para el tipo de documento en la respuesta.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoDocumentoResponse {
        @JsonProperty("id_tipo_documento")
        private Integer idTipoDocumento;

        @JsonProperty("desc_tipo_documento")
        private String descTipoDocumento;

        @JsonProperty("abrev_tipo_documento")
        private String abrevTipoDocumento;
    }
}