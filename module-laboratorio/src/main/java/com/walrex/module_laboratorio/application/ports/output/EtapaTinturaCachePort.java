package com.walrex.module_laboratorio.application.ports.output;

import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EtapaTinturaCachePort {
    Mono<Void> saveAll(String key, List<EtapaTintura> etapas);
    Flux<EtapaTintura> findAll(String key);
    Mono<Void> invalidate(String keyPattern);
}
