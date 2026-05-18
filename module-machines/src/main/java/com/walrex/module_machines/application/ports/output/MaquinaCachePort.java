package com.walrex.module_machines.application.ports.output;

import com.walrex.module_machines.domain.model.Maquina;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MaquinaCachePort {
    Mono<Void> saveAll(Integer idUbicacion, List<Maquina> maquinas);
    Mono<List<Maquina>> getAll(Integer idUbicacion);
}
