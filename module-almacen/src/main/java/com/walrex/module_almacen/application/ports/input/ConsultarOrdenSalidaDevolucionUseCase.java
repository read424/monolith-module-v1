package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ListadoOrdenSalidaDevolucionRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.PaginatedResponse;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.OrdenSalidaDevolucionResponse;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para consultar órdenes de salida por devolución.
 * Define el contrato para obtener el listado paginado de órdenes de salida con filtros.
 */
public interface ConsultarOrdenSalidaDevolucionUseCase {
    
    /**
     * Consulta el listado paginado de órdenes de salida por devolución aplicando los filtros especificados.
     * 
     * @param request filtros y parámetros de paginación para la consulta
     * @return Mono con el listado paginado de órdenes de salida por devolución
     */
    Mono<PaginatedResponse<OrdenSalidaDevolucionResponse>> consultarOrdenSalidaDevolucion(ListadoOrdenSalidaDevolucionRequest request);
} 