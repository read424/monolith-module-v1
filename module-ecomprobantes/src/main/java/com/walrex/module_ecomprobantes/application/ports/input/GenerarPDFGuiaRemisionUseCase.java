package com.walrex.module_ecomprobantes.application.ports.input;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para la generación de PDF de guías de remisión.
 */
public interface GenerarPDFGuiaRemisionUseCase {

    /**
     * Genera un PDF de guía de remisión para el comprobante especificado.
     * 
     * @param idComprobante ID del comprobante para generar la guía
     * @return Mono con los bytes del PDF generado
     */
    Mono<byte[]> generarPDFGuiaRemision(Integer idComprobante);
}