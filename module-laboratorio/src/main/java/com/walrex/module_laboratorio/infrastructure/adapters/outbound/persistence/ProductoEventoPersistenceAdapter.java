package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence;

import com.walrex.module_laboratorio.application.ports.output.ProductoEventoPersistencePort;
import com.walrex.module_laboratorio.domain.exceptions.ProductoEventoException;
import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.mapper.ProductoEventoPersistenceMapper;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository.ProductoEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductoEventoPersistenceAdapter implements ProductoEventoPersistencePort {

    private final ProductoEventoRepository repository;
    private final ProductoEventoPersistenceMapper mapper;

    @Override
    public Mono<ProductoEvento> save(ProductoEvento productoEvento) {
        return repository.save(mapper.toEntity(productoEvento))
                .map(mapper::toDomain)
                .onErrorMap(DuplicateKeyException.class,
                        error -> new ProductoEventoException(
                                "Ya existe un producto evento con ese nombre", "DUPLICATE_NAME"));
    }

    @Override
    public Mono<ProductoEvento> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<ProductoEvento> findAll(String search, int page, int size, Integer status) {
        long offset = (long) page * size;
        return repository.findAllPaged(search, offset, size, status)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> count(String search, Integer status) {
        return repository.countAll(search, status);
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return repository.existsByNormalizedNombre(nombre);
    }

    @Override
    public Mono<Boolean> existsByNombreExcludingId(String nombre, Integer id) {
        return repository.existsByNormalizedNombreExcludingId(nombre, id);
    }

    @Override
    public Mono<Void> logicalDelete(Integer id) {
        return repository.logicalDelete(id);
    }
}
