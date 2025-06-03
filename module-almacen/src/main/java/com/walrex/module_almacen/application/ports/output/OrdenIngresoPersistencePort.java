package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.OrdenIngresoDTO;
import reactor.core.publisher.Mono;

public interface OrdenIngresoPersistencePort {
    /**
     * Guarda una nueva orden de ingreso
     * @param ordenIngresoDTO datos de la orden de ingreso
     * @return la orden de ingreso guardada con su ID generado
     */
    Mono<OrdenIngresoDTO> guardarOrdenIngreso(OrdenIngresoDTO ordenIngresoDTO);

    /**
     * Busca una orden de ingreso por su ID
     * @param id identificador de la orden de ingreso
     * @return la orden de ingreso encontrada
     */
    Mono<OrdenIngresoDTO> buscarOrdenIngresoPorId(Long id);
}
