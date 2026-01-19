package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.dto.ProcesoDeclararItemDTO;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Caso de uso para declarar procesos incompletos de una partida.
 * Define el contrato para el procesamiento de declaraciones de procesos.
 */
public interface DeclararProcesoIncompletoUseCase {

    /**
     * Registra las declaraciones de procesos incompletos para una partida.
     *
     * @param procesos  Lista de procesos a declarar
     * @param idPartida ID de la partida asociada
     * @return Mono con el número de procesos declarados exitosamente
     */
    Mono<Integer> registrarDeclaracionProceso(List<ProcesoDeclararItemDTO> procesos, Integer idPartida);
}
