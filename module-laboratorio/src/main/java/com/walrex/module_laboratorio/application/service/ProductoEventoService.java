package com.walrex.module_laboratorio.application.service;

import com.walrex.module_laboratorio.application.ports.input.CreateProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.input.DeleteProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.input.GetProductoEventoByIdUseCase;
import com.walrex.module_laboratorio.application.ports.input.ListProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.input.UpdateProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.output.ProductoEventoPersistencePort;
import com.walrex.module_laboratorio.domain.exceptions.ProductoEventoException;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import com.walrex.module_laboratorio.domain.model.ProductoEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoEventoService implements
        CreateProductoEventoUseCase,
        GetProductoEventoByIdUseCase,
        ListProductoEventoUseCase,
        UpdateProductoEventoUseCase,
        DeleteProductoEventoUseCase {

    private final ProductoEventoPersistencePort persistencePort;

    @Override
    public Mono<ProductoEvento> create(ProductoEvento productoEvento) {
        normalizeNombre(productoEvento);
        validateNombre(productoEvento.getNombre());
        productoEvento.setStatus(1);

        return validateUniqueNombre(productoEvento.getNombre())
                .then(persistencePort.save(productoEvento))
                .doOnSuccess(saved -> log.info("Producto evento creado con ID: {}", saved.getId()));
    }

    @Override
    public Mono<ProductoEvento> getById(Integer id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ProductoEventoException(
                        "Producto evento no encontrado", "NOT_FOUND")));
    }

    @Override
    public Mono<PagedResponse<ProductoEvento>> listAll(String search, int page, int size, Integer status) {
        String normalizedSearch = search == null ? "" : search.trim();

        return Mono.zip(
                        persistencePort.findAll(normalizedSearch, page, size, status).collectList(),
                        persistencePort.count(normalizedSearch, status)
                )
                .map(tuple -> PagedResponse.of(tuple.getT1(), page + 1, size, tuple.getT2()));
    }

    @Override
    public Mono<ProductoEvento> update(Integer id, ProductoEvento productoEvento) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new ProductoEventoException(
                        "Producto evento no encontrado para actualizar", "NOT_FOUND")))
                .flatMap(existing -> {
                    merge(existing, productoEvento);
                    normalizeNombre(existing);
                    validateNombre(existing.getNombre());

                    return validateUniqueNombreExcludingId(existing.getNombre(), id)
                            .then(persistencePort.save(existing));
                });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        return persistencePort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ProductoEventoException(
                                "Producto evento no encontrado para eliminar", "NOT_FOUND"));
                    }
                    return persistencePort.logicalDelete(id).then();
                });
    }

    private Mono<Void> validateUniqueNombre(String nombre) {
        return persistencePort.existsByNombre(nombre)
                .flatMap(exists -> exists
                        ? Mono.error(new ProductoEventoException(
                                "Ya existe un producto evento con ese nombre", "DUPLICATE_NAME"))
                        : Mono.empty());
    }

    private Mono<Void> validateUniqueNombreExcludingId(String nombre, Integer id) {
        return persistencePort.existsByNombreExcludingId(nombre, id)
                .flatMap(exists -> exists
                        ? Mono.error(new ProductoEventoException(
                                "Ya existe un producto evento con ese nombre", "DUPLICATE_NAME"))
                        : Mono.empty());
    }

    private void merge(ProductoEvento existing, ProductoEvento incoming) {
        if (incoming.getNombre() != null) {
            existing.setNombre(incoming.getNombre());
        }
        if (incoming.getStatus() != null) {
            existing.setStatus(incoming.getStatus());
        }
    }

    private void normalizeNombre(ProductoEvento productoEvento) {
        if (productoEvento.getNombre() != null) {
            productoEvento.setNombre(productoEvento.getNombre().trim());
        }
    }

    private void validateNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ProductoEventoException("El nombre es obligatorio", "INVALID_NAME");
        }
    }
}
