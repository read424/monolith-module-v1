package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence;

import com.walrex.module_laboratorio.application.ports.output.RecetaPersistencePort;
import com.walrex.module_laboratorio.domain.model.Receta;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper.RecetaProjectionMapper;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository.RecetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecetaPersistenceAdapter implements RecetaPersistencePort {

    private final RecetaRepository repository;
    private final RecetaProjectionMapper mapper;

    @Override
    public Flux<Receta> findAll(String search, int page, int size) {
        long offset = (long) page * size;
        return repository.findAllPaged(search, offset, size)
                .map(p -> mapper.toDomain(p, List.of()));
    }

    @Override
    public Mono<Long> count(String search) {
        return repository.countAll(search);
    }

    @Override
    public Mono<Receta> findById(Integer id) {
        return Mono.zip(
                repository.findById(id),
                repository.getCurvasDiseno(id).collectList()
        ).map(tuple -> mapper.toDomain(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Receta> updateCurvaDiseno(Integer id, String curvaDiseno) {
        return repository.updateCurvaDiseno(id, curvaDiseno)
                .map(p -> mapper.toDomain(p, List.of()));
    }
}
