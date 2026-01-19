package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.dto.DetailProcesoProductionDTO;
import com.walrex.module_partidas.domain.model.dto.ItemProcessProductionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para procesamiento de declaraciones de procesos incompletos.
 * Define el contrato para operaciones de persistencia relacionadas con procesos de partidas.
 */
public interface ProcesarDeclaracionesPort {

    /**
     * Busca todos los procesos asociados a una partida específica.
     *
     * @param idPartida ID de la partida
     * @return Flux con los detalles de los procesos encontrados
     */
    Flux<DetailProcesoProductionDTO> findProcesosByIdPartida(Integer idPartida);

    /**
     * Guarda un proceso incompleto asociado a una partida.
     *
     * @param proceso   Datos del proceso a guardar
     * @param idPartida ID de la partida asociada
     * @return Mono con el ID del registro guardado
     */
    Mono<Integer> saveProcesoIncompletoByIdPartida(ItemProcessProductionDTO proceso, Integer idPartida);

    /**
     * Busca el primer ID de máquina disponible según el tipo de máquina.
     *
     * @param idTipoMaquina ID del tipo de máquina
     * @return Mono con el ID de la máquina encontrada
     */
    Mono<Integer> findFirstIdMachineByIdTipoMaquina(Integer idTipoMaquina);
}
