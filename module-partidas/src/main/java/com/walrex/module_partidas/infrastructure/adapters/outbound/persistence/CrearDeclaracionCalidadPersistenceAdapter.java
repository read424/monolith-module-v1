package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.CrearDeclaracionCalidadPort;
import com.walrex.module_partidas.domain.model.DeclaracionCalidad;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.DeclaracionCalidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CrearDeclaracionCalidadPersistenceAdapter implements CrearDeclaracionCalidadPort {

    private final DeclaracionCalidadRepository repository;

    @Override
    public Mono<DeclaracionCalidad> crear(DeclaracionCalidad declaracion) {
        return repository.insert(declaracion);
    }
}
