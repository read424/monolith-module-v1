package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.ProcesoPartida;

import reactor.core.publisher.Flux;

/**
 * Puerto de salida para consultar procesos de partida
 * Define el contrato para la consulta de procesos disponibles por partida
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface ConsultarProcesosPartidaPort {

    /**
     * Consulta todos los procesos de una partida específica
     * Incluye información sobre el estado, máquinas y rutas
     * 
     * @param idPartida ID de la partida a consultar
     * @return Flux de procesos de partida
     */
    Flux<ProcesoPartida> consultarProcesosPartida(Integer idPartida);
}
