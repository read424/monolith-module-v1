package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence;

import com.walrex.module_laboratorio.application.ports.output.EtapaTinturaPersistencePort;
import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper.EtapaTinturaPersistenceMapper;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository.EtapaTinturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EtapaTinturaPersistenceAdapter implements EtapaTinturaPersistencePort {

    private final EtapaTinturaRepository repository;
    private final EtapaTinturaPersistenceMapper mapper;

    @Override
    public Mono<EtapaTintura> save(EtapaTintura etapa) {
        return repository.save(mapper.toEntity(etapa))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<EtapaTintura> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<EtapaTintura> findAll(int page, int size) {
        long offset = (long) page * size;
        return repository.findAllActive(offset, size)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Integer id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }
}
