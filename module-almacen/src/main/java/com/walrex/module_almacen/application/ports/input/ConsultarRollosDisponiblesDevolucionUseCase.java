package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;

import reactor.core.publisher.Flux;

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
}