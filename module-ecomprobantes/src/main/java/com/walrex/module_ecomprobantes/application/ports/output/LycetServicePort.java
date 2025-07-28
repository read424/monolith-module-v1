package com.walrex.module_ecomprobantes.application.ports.output;

import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionRequest;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionResponse;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para el servicio de Lycet (envío de comprobantes
 * electrónicos).
 */
public interface LycetServicePort {

    /**
     * Envía una guía de remisión al servicio de Lycet.
     * 
     * @param request DTO con los datos de la guía de remisión
     * @return Mono con la respuesta estructurada del servicio
     */
    Mono<LycetGuiaRemisionResponse> enviarGuiaRemision(LycetGuiaRemisionRequest request);
}