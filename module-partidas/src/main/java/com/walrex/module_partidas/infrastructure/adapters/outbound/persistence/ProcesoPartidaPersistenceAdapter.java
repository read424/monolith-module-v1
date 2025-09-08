package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_partidas.application.ports.output.ConsultarProcesosPartidaPort;
import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper.ProcesoPartidaMapper;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.ProcesoPartidaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adaptador de persistencia para Procesos de Partida
 * Implementa el puerto de salida y se encarga de la comunicación con la base de
 * datos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcesoPartidaPersistenceAdapter implements ConsultarProcesosPartidaPort {

    private final ProcesoPartidaRepository repository;
    private final ProcesoPartidaMapper mapper;

    @Override
    public Flux<ProcesoPartida> consultarProcesosPartida(Integer idPartida) {
        log.debug("Consultando procesos para partida ID: {}", idPartida);

        return repository.findProcesosByPartida(idPartida)
                .map(mapper::toDomain)
                .doOnNext(proceso -> log.debug("Proceso procesado: ID={}, Proceso={}, Pendiente={}",
                        proceso.getIdPartida(), proceso.getNoProceso(), proceso.getIsPendiente()))
                .doOnComplete(() -> log.info("Consulta de procesos completada para partida ID: {}", idPartida))
                .doOnError(error -> log.error("Error consultando procesos para partida ID {}: {}",
                        idPartida, error.getMessage()));
    }
}
