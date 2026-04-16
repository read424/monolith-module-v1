package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence;

import com.walrex.module_laboratorio.application.ports.output.CurvaDisenoPersistencePort;
import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository.CurvaDisenoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CurvaDisenoPersistenceAdapter implements CurvaDisenoPersistencePort {

    private final CurvaDisenoRepository repository;

    @Override
    public Mono<CurvaDiseno> save(CurvaDiseno curvaDiseno) {
        return repository.save(curvaDiseno);
    }

    @Override
    public Mono<CurvaDiseno> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Flux<CurvaDiseno> findAll(String search, int page, int size) {
        return repository.findAll(search, page, size);
    }

    @Override
    public Mono<Long> countAll(String search) {
        return repository.countAll(search);
    }
}
