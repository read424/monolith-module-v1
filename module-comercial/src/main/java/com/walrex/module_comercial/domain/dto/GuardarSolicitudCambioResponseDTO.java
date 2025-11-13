package com.walrex.module_comercial.domain.dto;

import java.util.List;

/**
 * DTO de respuesta para el guardado exitoso de solicitud de cambio de servicio (Java 21 record).
 * Contiene la información resultante de la operación de guardado.
 *
 * Este record es inmutable y proporciona automáticamente:
 * - Constructor canónico
 * - Getters (sin prefijo 'get')
 * - equals(), hashCode(), toString()
 *
 * @param codOrdenProduccion Código de la orden de producción afectada
 * @param idOrdenProduccion ID de la orden de producción
 * @param idRuta ID de la ruta de producción
 * @param isProcess Indica si la solicitud está en proceso
 * @param pendingApproval Indica si la solicitud requiere aprobación pendiente
 * @param partidasAditionals Lista de partidas adicionales afectadas por el cambio
 */
public record GuardarSolicitudCambioResponseDTO(
    String codOrdenProduccion,
    Integer idOrdenProduccion,
    Integer idRuta,
    Boolean isProcess,
    Boolean pendingApproval,
    List<PartidaInfo> partidasAditionals
) {
    /**
     * Constructor compacto para validaciones opcionales.
     * Se ejecuta antes del constructor canónico.
     */
    public GuardarSolicitudCambioResponseDTO {
        // Validaciones opcionales si son necesarias
        // Por ejemplo: Objects.requireNonNull(idOrdenProduccion, "idOrdenProduccion no puede ser null");

        // Si partidasAditionals es null, lo convertimos a lista vacía para evitar NPE
        if (partidasAditionals == null) {
            partidasAditionals = List.of();
        }
    }

    /**
     * Factory method para crear una respuesta exitosa básica sin partidas adicionales.
     *
     * @param codOrdenProduccion Código de la orden de producción
     * @param idOrdenProduccion ID de la orden de producción
     * @param idRuta ID de la ruta
     * @param isProcess Indica si está en proceso
     * @param pendingApproval Indica si requiere aprobación
     * @return Nueva instancia de GuardarSolicitudCambioResponseDTO sin partidas adicionales
     */
    public static GuardarSolicitudCambioResponseDTO withoutPartidas(
            String codOrdenProduccion,
            Integer idOrdenProduccion,
            Integer idRuta,
            Boolean isProcess,
            Boolean pendingApproval) {
        return new GuardarSolicitudCambioResponseDTO(
                codOrdenProduccion,
                idOrdenProduccion,
                idRuta,
                isProcess,
                pendingApproval,
                List.of()
        );
    }
}