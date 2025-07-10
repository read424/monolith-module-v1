package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.OrdenSalidaDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ListadoOrdenSalidaDevolucionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para el acceso a datos de órdenes de salida por devolución.
 * Define el contrato para las operaciones de persistencia relacionadas con el listado paginado.
 */
public interface OrdenSalidaDevolucionPersistencePort {
    
    /**
     * Obtiene el listado paginado de órdenes de salida por devolución aplicando los filtros especificados.
     * 
     * @param request filtros y parámetros de paginación para la consulta
     * @return Flux con el listado paginado de órdenes de salida por devolución
     */
    Flux<OrdenSalidaDevolucionDTO> obtenerListadoOrdenSalidaDevolucion(ListadoOrdenSalidaDevolucionRequest request);
    
    /**
     * Cuenta el total de órdenes de salida por devolución que cumplen con los filtros especificados.
     * 
     * @param request filtros para la consulta
     * @return Mono con el total de registros
     */
    Mono<Long> contarOrdenSalidaDevolucion(ListadoOrdenSalidaDevolucionRequest request);
} 