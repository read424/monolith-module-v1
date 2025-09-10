package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.SuccessPartidaTacho;
import com.walrex.module_partidas.domain.model.dto.IngresoAlmacenDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para guardar el éxito de salida de tacho
 * Define el contrato para el caso de uso de procesamiento de rollos
 * seleccionados
 */
public interface SaveSuccessOutTachoUseCase {

    /**
     * Procesa la salida exitosa de tacho para una partida
     * Valida rollos disponibles, crea ingreso al próximo almacén y actualiza
     * estados
     * 
     * @param successPartidaTacho Datos de la partida tacho con rollos seleccionados
     * @return Mono<IngresoAlmacen> con los datos del ingreso al almacén y rollos procesados
     */
    Mono<IngresoAlmacenDTO> saveSuccessOutTacho(SuccessPartidaTacho successPartidaTacho);
}
