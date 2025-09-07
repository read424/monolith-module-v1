package com.walrex.module_partidas.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_partidas.application.ports.input.ConsultarProcesosPartidaUseCase;
import com.walrex.module_partidas.application.ports.output.ConsultarProcesosPartidaPort;
import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.domain.model.dto.ConsultarProcesosPartidaRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Servicio de aplicación para consultar procesos de partida
 * Implementa el caso de uso y orquesta la lógica de negocio
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarProcesosPartidaService implements ConsultarProcesosPartidaUseCase {

    private final ConsultarProcesosPartidaPort consultarProcesosPartidaPort;

    @Override
    public Flux<ProcesoPartida> consultarProcesosPartida(ConsultarProcesosPartidaRequest request) {
        log.info("Iniciando consulta de procesos para partida ID: {}", request.getIdPartida());

        // Validación básica del request
        if (request.getIdPartida() == null || request.getIdPartida() <= 0) {
            log.error("ID de partida inválido: {}", request.getIdPartida());
            return Flux.error(new IllegalArgumentException("ID de partida debe ser un valor válido mayor a 0"));
        }

        return consultarProcesosPartidaPort.consultarProcesosPartida(request.getIdPartida())
                .doOnNext(proceso -> log.debug("Proceso encontrado: ID={}, Proceso={}, Status={}, Pendiente={}",
                        proceso.getIdPartida(), proceso.getNoProceso(), proceso.getStatus(), proceso.getIsPendiente()))
                .doOnComplete(() -> log.info("Consulta de procesos completada exitosamente para partida ID: {}",
                        request.getIdPartida()))
                .doOnError(error -> log.error("Error en consulta de procesos para partida ID {}: {}",
                        request.getIdPartida(), error.getMessage()));
    }
}
