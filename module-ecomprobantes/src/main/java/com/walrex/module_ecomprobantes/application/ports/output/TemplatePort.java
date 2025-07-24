package com.walrex.module_ecomprobantes.application.ports.output;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para generación de templates
 * Define el contrato para generar HTML y PDFs usando templates
 */
public interface TemplatePort {

    /**
     * Genera un PDF a partir de un template y datos
     * 
     * @param templateName Nombre del template a usar
     * @param datos        Datos para el template
     * @return Mono con los bytes del PDF generado
     */
    Mono<byte[]> generarPDF(String templateName, Object datos);

    /**
     * Genera HTML a partir de un template y datos
     * 
     * @param templateName Nombre del template a usar
     * @param datos        Datos para el template
     * @return Mono con el HTML generado
     */
    Mono<String> generarHTML(String templateName, Object datos);
}