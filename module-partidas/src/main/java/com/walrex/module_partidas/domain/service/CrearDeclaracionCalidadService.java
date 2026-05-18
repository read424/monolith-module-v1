package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.CrearDeclaracionCalidadUseCase;
import com.walrex.module_partidas.application.ports.output.CrearDeclaracionCalidadPort;
import com.walrex.module_partidas.domain.model.DeclaracionCalidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CrearDeclaracionCalidadService implements CrearDeclaracionCalidadUseCase {

    private final CrearDeclaracionCalidadPort crearDeclaracionCalidadPort;

    @Override
    public Mono<DeclaracionCalidad> crear(DeclaracionCalidad declaracion) {
        return crearDeclaracionCalidadPort.crear(declaracion);
    }
}
