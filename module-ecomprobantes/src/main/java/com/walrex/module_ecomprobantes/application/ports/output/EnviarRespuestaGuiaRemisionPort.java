package com.walrex.module_ecomprobantes.application.ports.output;

import com.walrex.avro.schemas.GuiaRemisionRemitenteResponse;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para enviar respuestas de guía de remisión al topic de
 * respuesta
 */
public interface EnviarRespuestaGuiaRemisionPort {

    /**
     * Envía una respuesta de procesamiento de guía de remisión
     * 
     * @param response      Respuesta con el resultado del procesamiento
     * @param correlationId ID de correlación para tracking
     * @return Mono que completa cuando la respuesta ha sido enviada
     */
    Mono<Void> enviarRespuesta(GuiaRemisionRemitenteResponse response, String correlationId);
}