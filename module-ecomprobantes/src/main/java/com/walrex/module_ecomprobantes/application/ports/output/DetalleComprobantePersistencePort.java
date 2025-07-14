package com.walrex.module_ecomprobantes.application.ports.output;

import java.util.List;

import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de detalles de comprobantes
 * 
 * Define las operaciones necesarias para gestionar los detalles (líneas)
 * de los comprobantes siguiendo arquitectura hexagonal
 */
public interface DetalleComprobantePersistencePort {

    /**
     * Crea detalles de comprobante a partir de items de guía de remisión
     * 
     * @param items         Lista de items del mensaje Avro
     * @param idComprobante ID del comprobante padre
     * @return Mono que completa cuando todos los detalles se han creado
     */
    Mono<Void> crearDetallesComprobante(List<ItemGuiaRemisionRemitenteMessage> items, Long idComprobante);

    /**
     * Busca detalles de un comprobante por su ID
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el número de detalles creados
     */
    Mono<Long> contarDetallesPorComprobante(Long idComprobante);

    /**
     * Elimina todos los detalles de un comprobante
     * 
     * @param idComprobante ID del comprobante
     * @return Mono que completa cuando se han eliminado los detalles
     */
    Mono<Void> eliminarDetallesPorComprobante(Long idComprobante);
}