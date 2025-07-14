package com.walrex.module_ecomprobantes.application.ports.input;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;

import reactor.core.publisher.Mono;

/**
 * Use Case para procesar guías de remisión recibidas desde el módulo de almacén
 */
public interface ProcesarGuiaRemisionUseCase {

    /**
     * Procesa una guía de remisión creando el comprobante correspondiente
     * y enviando la respuesta al topic de respuesta
     * 
     * @param message       Mensaje con datos de la guía de remisión
     * @param correlationId ID de correlación para tracking
     * @return Mono que completa cuando el procesamiento termina
     */
    Mono<Void> procesarGuiaRemision(CreateGuiaRemisionRemitenteMessage message, String correlationId);
}