package com.walrex.module_comercial.application.ports.output;

import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioResponseDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.SolicitudCambioServicioPartidaDTO;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de solicitudes de cambio de servicio.
 * Define el contrato para operaciones de persistencia en la base de datos.
 */
public interface SolicitudCambioPersistencePort {

    /**
     * Guarda una solicitud de cambio de servicio en la base de datos.
     * Incluye el guardado de la solicitud principal y las partidas adicionales si existen.
     *
     * @param solicitudDTO DTO enriquecido con datos reales de BD (valores OLD y NEW)
     * @return Mono con el resultado de la operación, incluyendo información de la orden,
     *         estado de proceso, aprobación pendiente y partidas adicionales afectadas
     */
    Mono<GuardarSolicitudCambioResponseDTO> guardarSolicitudCambio(SolicitudCambioServicioPartidaDTO solicitudDTO);
}