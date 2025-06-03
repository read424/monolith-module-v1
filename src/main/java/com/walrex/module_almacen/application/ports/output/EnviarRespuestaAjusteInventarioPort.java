package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.ResponseAjusteInventoryDTO;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para enviar respuestas de ajuste de inventario
 * al módulo de almacen-inventario.
 */
public interface EnviarRespuestaAjusteInventarioPort {
    /**
     * Envía una respuesta de ajuste de inventario.
     *
     * @param correlationId ID de correlación para vincular con la solicitud original
     * @param respuesta La respuesta del ajuste de inventario
     * @return Un Mono que completa cuando la respuesta ha sido enviada
     */
    Mono<Void> enviarRespuesta(String correlationId, ResponseAjusteInventoryDTO respuesta);
}
