package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.ConsultarRollosDisponiblesResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para consultar rollos disponibles para devolución
 */
public interface ConsultarRollosDisponiblesDevolucionUseCase {

    /**
     * Consulta rollos disponibles para devolución por cliente y artículo
     * 
     * @param idCliente  ID del cliente
     * @param idArticulo ID del artículo
     * @return Flux de rollos disponibles para devolución
     */
    Flux<RolloDisponibleDevolucionDTO> consultarRollosDisponibles(Integer idCliente, Integer idArticulo);

    /**
     * Consulta rollos disponibles y devuelve la respuesta completa
     * 
     * @param idCliente  ID del cliente
     * @param idArticulo ID del artículo
     * @return Mono con la respuesta completa de rollos disponibles
     */
    Mono<ConsultarRollosDisponiblesResponse> consultarRollosDisponiblesResponse(Integer idCliente, Integer idArticulo);
}