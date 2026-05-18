package com.walrex.module_machines.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_machines.domain.model.Maquina;
import reactor.core.publisher.Flux;

public interface MaquinaRepository {
    Flux<Maquina> findAllByUbicacion(Integer idUbicacion);
}
