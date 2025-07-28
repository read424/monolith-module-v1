package com.walrex.module_ecomprobantes.domain.model.dto.lycet;

import lombok.*;

/**
 * DTO para errores de validación de Lycet (HTTP 400).
 * 
 * Schema de respuesta:
 * [
 * {
 * "message": "string",
 * "field": "string"
 * }
 * ]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LycetValidationError {

    /**
     * Mensaje de error de validación
     */
    private String message;

    /**
     * Campo que causó el error de validación
     */
    private String field;
}