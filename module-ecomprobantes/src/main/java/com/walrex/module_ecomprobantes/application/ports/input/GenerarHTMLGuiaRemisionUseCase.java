package com.walrex.module_ecomprobantes.application.ports.input;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para generar HTML de guías de remisión.
 * Define el contrato para la generación de documentos HTML renderizados.
 */
public interface GenerarHTMLGuiaRemisionUseCase {

    /**
     * Genera el HTML renderizado de una guía de remisión por ID de comprobante.
     * 
     * @param idComprobante ID del comprobante a generar
     * @return Mono con el HTML generado como String
     */
    Mono<String> generarHTMLGuiaRemision(Integer idComprobante);
}