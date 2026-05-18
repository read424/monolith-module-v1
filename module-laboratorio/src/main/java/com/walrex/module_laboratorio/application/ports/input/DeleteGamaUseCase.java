package com.walrex.module_laboratorio.application.ports.input;
import reactor.core.publisher.Mono;
public interface DeleteGamaUseCase {
    Mono<Void> delete(Integer id);
}
