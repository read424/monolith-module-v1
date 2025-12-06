package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.ProcesarDeclaracionesPort;
import com.walrex.module_partidas.domain.model.dto.DetailProcesoProductionDTO;
import com.walrex.module_partidas.domain.model.dto.ItemProcessProductionDTO;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper.DetailProcesoProductionMapper;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.PartidaProcesosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para procesamiento de declaraciones de procesos incompletos.
 * Implementa el puerto de salida y se encarga de la comunicación con la base de datos.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcesarDeclaracionesPersistenceAdapter implements ProcesarDeclaracionesPort {

    private final PartidaProcesosRepository repository;
    private final DetailProcesoProductionMapper mapper;

    @Override
    public Flux<DetailProcesoProductionDTO> findProcesosByIdPartida(Integer idPartida) {
        log.debug("Buscando procesos incompletos para partida ID: {}", idPartida);

        return repository.findProcesosIncompletos(idPartida)
            .map(mapper::toDTO)
            .doOnNext(proceso -> log.debug("Proceso procesado: idProceso={}, idPartidaMaquina={}",
                proceso.getIdProceso(), proceso.getIdPartidaMaquina()))
            .doOnComplete(() -> log.info("Búsqueda de procesos completada para partida ID: {}", idPartida))
            .doOnError(error -> log.error("Error buscando procesos para partida ID {}: {}",
                idPartida, error.getMessage()));
    }

    @Override
    public Mono<Integer> saveProcesoIncompletoByIdPartida(ItemProcessProductionDTO proceso, Integer idPartida) {
        log.debug("Guardando proceso incompleto para partida ID: {}", idPartida);
        log.debug("Datos del proceso: idProceso={}, idDetRuta={}, idTipoMaquina={}, idMaquina={}",
            proceso.getIdProceso(), proceso.getIdDetRuta(), proceso.getIdTipoMaquina(), proceso.getIdMaquina());

        return repository.saveProcesoIncompleto(proceso, idPartida)
            .doOnSuccess(id -> log.info("Proceso incompleto guardado exitosamente con ID: {} para partida: {}",
                id, idPartida))
            .doOnError(error -> log.error("Error guardando proceso incompleto para partida ID {}: {}",
                idPartida, error.getMessage()));
    }

    @Override
    public Mono<Integer> findFirstIdMachineByIdTipoMaquina(Integer idTipoMaquina) {
        log.debug("Buscando primera máquina disponible para tipo: {}", idTipoMaquina);

        return repository.findFirstIdMachineByTipoMaquina(idTipoMaquina)
            .doOnSuccess(id -> log.info("Máquina encontrada: ID={} para tipo: {}", id, idTipoMaquina))
            .doOnError(error -> log.error("Error buscando máquina para tipo {}: {}",
                idTipoMaquina, error.getMessage()));
    }
}