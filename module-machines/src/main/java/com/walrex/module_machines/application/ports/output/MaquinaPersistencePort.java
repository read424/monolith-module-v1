package com.walrex.module_machines.application.ports.output;

import com.walrex.module_machines.domain.model.Maquina;
import reactor.core.publisher.Flux;

public interface MaquinaPersistencePort {
    Flux<Maquina> findAllByUbicacion(Integer idUbicacion);
}
