package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.DeclararProcesoIncompletoUseCase;
import com.walrex.module_partidas.application.ports.output.ProcesarDeclaracionesPort;
import com.walrex.module_partidas.domain.model.dto.ProcesoDeclararItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio de dominio para declarar procesos incompletos.
 * Implementa el caso de uso y orquesta la lógica de negocio.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeclararProcesoIncompletoService implements DeclararProcesoIncompletoUseCase {

    private final ProcesarDeclaracionesPort procesarDeclaracionesPort;

    @Override
    public Mono<Integer> registrarDeclaracionProceso(List<ProcesoDeclararItemDTO> procesos, Integer idPartida) {
        log.info("Iniciando registro de declaración de procesos incompletos para partida ID: {}", idPartida);
        log.info("Total de procesos a declarar: {}", procesos.size());

        // TODO: Implementar lógica de negocio

        return Mono.just(procesos.size());
    }
}