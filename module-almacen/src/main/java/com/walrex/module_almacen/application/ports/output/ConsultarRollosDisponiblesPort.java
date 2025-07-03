package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;

import reactor.core.publisher.Flux;

/**
 * Puerto de salida para consultar rollos disponibles para devolución
 */
public interface ConsultarRollosDisponiblesPort {

    /**
     * Consulta rollos disponibles para devolución filtrados por cliente y artículo
     * 
     * @param idCliente  ID del cliente
     * @param idArticulo ID del artículo
     * @return Flux de rollos disponibles para devolución
     */
    Flux<RolloDisponibleDevolucionDTO> buscarRollosDisponibles(Integer idCliente, Integer idArticulo);
}