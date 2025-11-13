package com.walrex.module_comercial.domain.dto;

/**
 * DTO de información de partida (Java 21 record).
 * Representa datos básicos de una partida adicional afectada por la solicitud de cambio.
 *
 * Este record es inmutable y proporciona automáticamente:
 * - Constructor canónico
 * - Getters (sin prefijo 'get')
 * - equals(), hashCode(), toString()
 */
public record PartidaInfo(
    String codPartida,
    Integer idPartida,
    Integer status
) {
    /**
     * Constructor compacto para validaciones opcionales.
     * Se ejecuta antes del constructor canónico.
     */
    public PartidaInfo {
        // Validaciones opcionales si son necesarias
        // Por ejemplo: Objects.requireNonNull(idPartida, "idPartida no puede ser null");
    }
}