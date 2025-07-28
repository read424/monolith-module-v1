package com.walrex.module_ecomprobantes.application.ports.output;

import com.walrex.module_ecomprobantes.domain.model.dto.lycet.LycetGuiaRemisionResponse;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para actualizar la información del comprobante
 * después de recibir la respuesta de Lycet.
 */
public interface ActualizarComprobanteLycetPort {

    /**
     * Actualiza la información del comprobante con la respuesta de Lycet.
     * 
     * @param idComprobante ID del comprobante a actualizar
     * @param response      Respuesta de Lycet con la información de SUNAT
     * @return Mono con el resultado de la actualización
     */
    Mono<Void> actualizarComprobanteConRespuestaLycet(Long idComprobante, LycetGuiaRemisionResponse response);
}