package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence;

import com.walrex.module_laboratorio.application.ports.output.GamaPersistencePort;
import com.walrex.module_laboratorio.domain.exceptions.GamaException;
import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper.GamaPersistenceMapper;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository.GamaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GamaPersistenceAdapter implements GamaPersistencePort {

    private final GamaRepository repository;
    private final GamaPersistenceMapper mapper;

    @Override
    public Mono<Gama> save(Gama gama) {
        return repository.save(mapper.toEntity(gama))
                .map(mapper::toDomain)
                .onErrorMap(DuplicateKeyException.class,
                        error -> new GamaException("Ya existe una gama con ese nombre", "DUPLICATE_NAME"));
    }

    @Override
    public Mono<Gama> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Gama> findAll(int page, int size) {
        long offset = (long) page * size;
        return repository.findAllPaged(offset, size)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Gama> findAllActive(int page, int size) {
        long offset = (long) page * size;
        return repository.findAllActivePaged(offset, size)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countAll() {
        return repository.countAll();
    }

    @Override
    public Mono<Long> countAllActive() {
        return repository.countAllActive();
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.existsByNormalizedName(name);
    }

    @Override
    public Mono<Boolean> existsByNameExcludingId(String name, Integer id) {
        return repository.existsByNormalizedNameExcludingId(name, id);
    }

    @Override
    public Mono<Void> logicalDelete(Integer id) {
        return repository.logicalDelete(id);
    }
}
