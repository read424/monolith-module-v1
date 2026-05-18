package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.DeclaracionCalidad;
import reactor.core.publisher.Mono;

public interface CrearDeclaracionCalidadPort {
    Mono<DeclaracionCalidad> crear(DeclaracionCalidad declaracion);
}
