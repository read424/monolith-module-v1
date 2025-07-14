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

    /**
     * Busca un comprobante por su ID (sin detalles)
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el comprobante encontrado
     */
    Mono<ComprobanteDTO> buscarComprobantePorId(Long idComprobante);

    /**
     * Busca un comprobante por su ID incluyendo sus detalles
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el comprobante completo con detalles
     */
    Mono<ComprobanteDTO> buscarComprobanteConDetallesPorId(Long idComprobante);

    /**
     * Actualiza el estado de un comprobante
     * 
     * @param idComprobante ID del comprobante
     * @param nuevoEstado   Nuevo estado del comprobante
     * @return Mono que completa cuando la actualización termina
     */
    Mono<Void> actualizarEstadoComprobante(Long idComprobante, Integer nuevoEstado);

    /**
     * Actualiza un comprobante completo incluyendo sus detalles
     * 
     * @param comprobante ComprobanteDTO con datos actualizados y detalles
     * @return Mono con el comprobante actualizado
     */
    Mono<ComprobanteDTO> actualizarComprobanteCompleto(ComprobanteDTO comprobante);
}