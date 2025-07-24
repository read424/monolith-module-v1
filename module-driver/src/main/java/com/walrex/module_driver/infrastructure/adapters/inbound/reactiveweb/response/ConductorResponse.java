package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.response;

import lombok.*;

/**
 * DTO de respuesta para los datos del conductor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConductorResponse {

    private Long idConductor;
    private TipoDocumentoResponse tipoDocumento;
    private String numeroDocumento;
    private String apellidos;
    private String nombres;
    private String licencia;

    /**
     * DTO para el tipo de documento en la respuesta.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoDocumentoResponse {
        private Integer idTipoDocumento;
        private String descTipoDocumento;
    }
}