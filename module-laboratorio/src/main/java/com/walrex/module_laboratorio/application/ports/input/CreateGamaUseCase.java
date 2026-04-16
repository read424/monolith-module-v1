package com.walrex.module_laboratorio.application.ports.input;
import com.walrex.module_laboratorio.domain.model.Gama;
import reactor.core.publisher.Mono;
public interface CreateGamaUseCase {
    Mono<Gama> create(Gama gama);
}
