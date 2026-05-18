package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.DeclaracionCalidad;
import reactor.core.publisher.Mono;

public interface CrearDeclaracionCalidadUseCase {
    Mono<DeclaracionCalidad> crear(DeclaracionCalidad declaracion);
}
