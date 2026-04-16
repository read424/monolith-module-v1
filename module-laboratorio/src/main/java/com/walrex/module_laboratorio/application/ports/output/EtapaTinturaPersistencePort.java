package com.walrex.module_laboratorio.application.ports.output;

import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EtapaTinturaPersistencePort {
    Mono<EtapaTintura> save(EtapaTintura etapa);
    Mono<EtapaTintura> findById(Integer id);
    Flux<EtapaTintura> findAll(int page, int size);
    Mono<Void> deleteById(Integer id);
    Mono<Boolean> existsById(Integer id);
}
