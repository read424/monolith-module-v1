package com.walrex.module_ecomprobantes.application.ports.output;

import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionCabeceraProjection;
import com.walrex.module_ecomprobantes.domain.model.dto.GuiaRemisionDataDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para obtener datos de guías de remisión.
 * Define el contrato para el acceso a datos de guías de remisión.
 */
public interface GuiaRemisionDataPort {

    /**
     * Obtiene los datos completos de una guía de remisión por ID de comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con los datos de la guía de remisión
     */
    Mono<GuiaRemisionDataDTO> obtenerDatosGuiaRemision(Integer idComprobante);

    /**
     * Obtiene la proyección de cabecera de guía de remisión con detalles por ID de
     * comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con la proyección de cabecera y detalles de la guía de remisión
     */
    Mono<GuiaRemisionCabeceraProjection> obtenerProyeccionGuiaRemision(Integer idComprobante);
}