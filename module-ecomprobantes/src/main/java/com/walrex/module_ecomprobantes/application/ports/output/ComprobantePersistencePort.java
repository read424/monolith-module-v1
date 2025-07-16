package com.walrex.module_ecomprobantes.application.ports.output;

import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de comprobantes
 */
public interface ComprobantePersistencePort {

    /**
     * Crea un nuevo comprobante en la base de datos
     * 
     * @param comprobante Datos del comprobante a crear
     * @return Mono con el comprobante creado incluyendo su ID generado
     */
    Mono<ComprobanteDTO> crearComprobante(ComprobanteDTO comprobante);

}