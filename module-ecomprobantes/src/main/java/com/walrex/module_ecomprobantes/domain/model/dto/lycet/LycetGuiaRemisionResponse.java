package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

/**
 * DTO para la respuesta de guía de remisión de la API de Lycet.
 * Basado en la documentación de Lycet y la estructura de respuesta de SUNAT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetGuiaRemisionResponse {

    /**
     * Indica si la operación fue exitosa
     */
    private Boolean success;

    /**
     * Mensaje de respuesta
     */
    private String message;

    /**
     * Código de respuesta de SUNAT
     */
    private String sunatCode;

    /**
     * Descripción del código de respuesta
     */
    private String sunatDescription;

    /**
     * Número de comprobante procesado
     */
    private String comprobante;

    /**
     * Serie del comprobante
     */
    private String serie;

    /**
     * Correlativo del comprobante
     */
    private String correlativo;

    /**
     * Número de comprobante completo (serie-correlativo)
     */
    private String numeroComprobante;

    /**
     * Fecha de emisión del comprobante
     */
    private LocalDateTime fechaEmision;

    /**
     * Hash del comprobante (para verificación)
     */
    private String hash;

    /**
     * XML firmado del comprobante (base64)
     */
    private String xmlFirmado;

    /**
     * PDF del comprobante (base64)
     */
    private String pdf;

    /**
     * CDR (Constancia de Recepción) de SUNAT (base64)
     */
    private String cdr;

    /**
     * Errores específicos si los hay
     */
    private List<LycetError> errors;

    /**
     * Información adicional del comprobante
     */
    private LycetComprobanteInfo comprobanteInfo;

    /**
     * Timestamp de la respuesta
     */
    private LocalDateTime timestamp;

    /**
     * Respuesta específica de SUNAT según el schema de Lycet
     */
    private SunatResponse sunatResponse;

    /**
     * DTO para errores específicos de Lycet
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LycetError {
        private String code;
        private String message;
        private String field;
        private String value;
    }

    /**
     * DTO para información adicional del comprobante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LycetComprobanteInfo {
        private String tipoDocumento;
        private String estado;
        private String observaciones;
        private LocalDateTime fechaProcesamiento;
        private String numeroTicket; // Para casos de procesamiento asíncrono
    }

    /**
     * DTO para la respuesta específica de SUNAT según el schema de Lycet
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SunatResponse {
        private Boolean success;
        private SunatError error;
        private String ticket;
    }

    /**
     * DTO para errores de SUNAT
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SunatError {
        private String code;
        private String message;
    }
}