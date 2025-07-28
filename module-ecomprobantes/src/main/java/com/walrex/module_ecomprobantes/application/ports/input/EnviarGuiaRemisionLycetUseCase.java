package com.walrex.module_ecomprobantes.application.ports.input;

import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionResponse;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para el envío de guías de remisión a Lycet.
 */
public interface EnviarGuiaRemisionLycetUseCase {

    /**
     * Envía una guía de remisión al servicio de Lycet.
     * 
     * @param idComprobante ID del comprobante a enviar
     * @return Mono con la respuesta estructurada del servicio de Lycet
     */
    Mono<LycetGuiaRemisionResponse> enviarGuiaRemisionLycet(Integer idComprobante);
}