package com.walrex.module_laboratorio.application.ports.output;
import com.walrex.module_laboratorio.domain.model.Gama;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public interface GamaPersistencePort {
    Mono<Gama> save(Gama gama);
    Mono<Gama> findById(Integer id);
    Flux<Gama> findAll(int page, int size);
    Flux<Gama> findAllActive(int page, int size);
    Mono<Long> countAll();
    Mono<Long> countAllActive();
    Mono<Boolean> existsById(Integer id);
    Mono<Boolean> existsByName(String name);
    Mono<Boolean> existsByNameExcludingId(String name, Integer id);
    Mono<Void> logicalDelete(Integer id);
}
