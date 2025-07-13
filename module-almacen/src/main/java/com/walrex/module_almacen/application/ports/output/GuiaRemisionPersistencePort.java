package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;

import reactor.core.publisher.Mono;

public interface GuiaRemisionPersistencePort {

    /**
     * Genera una guía de remisión actualizando los datos en devolucion_servicios
     * y la fecha de entrega en ordensalida
     */
    Mono<GuiaRemisionGeneradaDTO> generarGuiaRemision(GuiaRemisionGeneradaDTO request);

    /**
     * Verifica si una orden de salida existe y es válida para generar guía
     */
    Mono<Boolean> validarOrdenSalidaParaGuia(Long idOrdenSalida);
}