package com.walrex.module_ecomprobantes.application.ports.output;

import java.io.ByteArrayOutputStream;

import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionDataDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para la generación de templates y PDFs.
 * Define el contrato para la generación de documentos usando templates.
 */
public interface GuiaRemisionTemplatePort {

    /**
     * Genera un PDF a partir de los datos de la guía de remisión.
     * 
     * @param data Datos de la guía de remisión
     * @return Mono con el PDF generado como ByteArrayOutputStream
     */
    Mono<ByteArrayOutputStream> generarPDF(GuiaRemisionDataDTO data);

    /**
     * Genera el HTML del template con los datos proporcionados.
     * 
     * @param data Datos de la guía de remisión
     * @return Mono con el HTML generado como String
     */
    Mono<String> generarHTML(GuiaRemisionDataDTO data);
}