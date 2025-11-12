package com.walrex.module_comercial.application.ports.input;

import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioRequestDTO;
import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioResponseDTO;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para guardar solicitud de cambio de servicio.
 * Define el contrato para el caso de uso de negocio.
 *
 * Responsabilidad: Definir la operación de guardar solicitud de cambio
 * según las reglas de negocio del módulo comercial.
 */
public interface GuardarSolicitudCambioServicioUseCase {

    /**
     * Guarda una solicitud de cambio de servicio en el sistema.
     * Este caso de uso coordina la validación y persistencia de la solicitud.
     *
     * @param request DTO con los datos completos de la solicitud de cambio de servicio
     * @return Mono con el resultado de la operación, conteniendo información de la orden de producción,
     *         estado del proceso, aprobaciones pendientes y partidas adicionales afectadas.
     *         En caso de error, el Mono emitirá una señal de error con la excepción correspondiente.
     *
     * @throws IllegalArgumentException si los datos de entrada no son válidos
     * @throws RuntimeException si ocurre un error durante la persistencia
     */
    Mono<GuardarSolicitudCambioResponseDTO> guardarSolicitudCambioServicio(GuardarSolicitudCambioRequestDTO request);
}