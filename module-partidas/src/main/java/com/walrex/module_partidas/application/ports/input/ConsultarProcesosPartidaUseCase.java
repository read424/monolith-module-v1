package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.domain.model.dto.ConsultarProcesosPartidaRequest;

import reactor.core.publisher.Flux;

/**
 * Puerto de entrada para consultar procesos de partida
 * Define el contrato para el caso de uso de consulta de procesos por partida
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface ConsultarProcesosPartidaUseCase {

    /**
     * Consulta todos los procesos de una partida específica
     * Incluye información sobre el estado, máquinas y rutas
     * 
     * @param request Criterios de consulta (ID de partida)
     * @return Flux de procesos de partida
     */
    Flux<ProcesoPartida> consultarProcesosPartida(ConsultarProcesosPartidaRequest request);
}
