package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.DeclinePartidaTacho;
import com.walrex.module_partidas.domain.model.dto.IngresoAlmacenDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para declinar salida de tacho
 * Define el contrato para el caso de uso de rechazo de salida de tacho
 * con almacén destino fijo (ID = 6)
 */
public interface DeclineOutTachoUseCase {

    /**
     * Procesa el rechazo de salida de tacho para una partida
     * Valida rollos disponibles, crea ingreso al almacén de rechazo (ID = 6) 
     * y actualiza estados
     * 
     * @param declinePartidaTacho Datos de la partida tacho con motivo de rechazo y personal supervisor
     * @return Mono<IngresoAlmacenDTO> con los datos del ingreso al almacén de rechazo y rollos procesados
     */
    Mono<IngresoAlmacenDTO> declineOutTacho(DeclinePartidaTacho declinePartidaTacho);
}
