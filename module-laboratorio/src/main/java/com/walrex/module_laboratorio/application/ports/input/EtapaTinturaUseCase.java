package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EtapaTinturaUseCase {
    Mono<EtapaTintura> create(EtapaTintura etapa);
    Mono<EtapaTintura> findById(Integer id);
    Flux<EtapaTintura> findAll(int page, int size);
    Mono<EtapaTintura> update(Integer id, EtapaTintura etapa);
    Mono<Void> delete(Integer id);
}
