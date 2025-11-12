package com.walrex.module_comercial.application.ports.input;

import com.walrex.module_comercial.domain.dto.OrdenProductionPartidaResponseDTO;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para obtener orden de producción por partida
 */
public interface GetOrdenProduccionPartidaUseCase {

    /**
     * Obtiene la orden de producción filtrada por ID de partida
     *
     * @param idPartida ID de la partida
     * @return Mono con la información de la orden de producción con partidas
     */
    Mono<OrdenProductionPartidaResponseDTO> getInfoOrdenProduccionPartida(Integer idPartida);
}
