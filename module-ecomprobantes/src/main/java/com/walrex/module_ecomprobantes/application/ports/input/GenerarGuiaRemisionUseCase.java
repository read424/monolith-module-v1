package com.walrex.module_ecomprobantes.application.ports.input;

import java.io.ByteArrayOutputStream;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para generar guías de remisión.
 * Define el contrato para la generación de documentos PDF.
 */
public interface GenerarGuiaRemisionUseCase {

    /**
     * Genera una guía de remisión en formato PDF por ID de comprobante.
     * 
     * @param idComprobante ID del comprobante a generar
     * @return Mono con el PDF generado como ByteArrayOutputStream
     */
    Mono<ByteArrayOutputStream> generarGuiaRemision(Integer idComprobante);
}