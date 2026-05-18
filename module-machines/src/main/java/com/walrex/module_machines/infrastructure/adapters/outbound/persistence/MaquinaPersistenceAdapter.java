package com.walrex.module_machines.infrastructure.adapters.outbound.persistence;

import com.walrex.module_machines.application.ports.output.MaquinaPersistencePort;
import com.walrex.module_machines.domain.model.Maquina;
import com.walrex.module_machines.infrastructure.adapters.outbound.persistence.repository.MaquinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class MaquinaPersistenceAdapter implements MaquinaPersistencePort {

    private final MaquinaRepository repository;

    @Override
    public Flux<Maquina> findAllByUbicacion(Integer idUbicacion) {
        return repository.findAllByUbicacion(idUbicacion);
    }
}
