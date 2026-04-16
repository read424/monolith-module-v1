package com.walrex.module_laboratorio.application.service;

import com.walrex.module_laboratorio.application.ports.input.*;
import com.walrex.module_laboratorio.application.ports.output.GamaCacheNamespacePort;
import com.walrex.module_laboratorio.application.ports.output.GamaCachePort;
import com.walrex.module_laboratorio.application.ports.output.GamaPersistencePort;
import com.walrex.module_laboratorio.domain.exceptions.GamaException;
import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamaService implements 
    CreateGamaUseCase, 
    GetGamaByIdUseCase, 
    ListGamasUseCase, 
    ListActiveGamasUseCase, 
    UpdateGamaUseCase, 
    DeleteGamaUseCase {

    private final GamaPersistencePort persistencePort;
    private final GamaCachePort cachePort;
    private final GamaCacheNamespacePort namespacePort;

    @Override
    public Mono<Gama> create(Gama gama) {
        normalizeName(gama);
        gama.setStatus(1);
        gama.setCreatedAt(OffsetDateTime.now());
        gama.setUpdatedAt(OffsetDateTime.now());
        return validateUniqueName(gama.getName())
            .then(persistencePort.save(gama))
            .flatMap(saved -> namespacePort.incrementVersion()
                .thenReturn(saved))
            .doOnSuccess(s -> log.info("Gama creada con ID: {}", s.getId()));
    }

    @Override
    public Mono<Gama> getById(Integer id) {
        return namespacePort.getCurrentVersion()
            .flatMap(version -> {
                String key = "gama:v" + version + ":" + id;
                return cachePort.getById(key)
                    .switchIfEmpty(persistencePort.findById(id)
                        .flatMap(gama -> cachePort.saveById(key, gama).thenReturn(gama)));
            })
            .switchIfEmpty(Mono.error(new GamaException("Gama no encontrada", "NOT_FOUND")));
    }

    private static final String CACHE_SCHEMA = "v2";

    @Override
    public Mono<PagedResponse<Gama>> listAll(int page, int size) {
        return namespacePort.getCurrentVersion()
            .flatMap(version -> {
                String key = "gama:" + CACHE_SCHEMA + ":v" + version + ":list:all:p" + page + ":s" + size;
                return cachePort.getPaged(key)
                    .onErrorResume(e -> {
                        log.warn("Cache read error (key={}): {} — fallback a BD", key, e.getMessage());
                        return Mono.empty();
                    })
                    .switchIfEmpty(fetchFromDbAndCache(key, page, size,
                            persistencePort.findAll(page, size),
                            persistencePort.countAll()));
            });
    }

    @Override
    public Mono<PagedResponse<Gama>> listActive(int page, int size) {
        return namespacePort.getCurrentVersion()
            .flatMap(version -> {
                String key = "gama:" + CACHE_SCHEMA + ":v" + version + ":list:active:p" + page + ":s" + size;
                return cachePort.getPaged(key)
                    .onErrorResume(e -> {
                        log.warn("Cache read error (key={}): {} — fallback a BD", key, e.getMessage());
                        return Mono.empty();
                    })
                    .switchIfEmpty(fetchFromDbAndCache(key, page, size,
                            persistencePort.findAllActive(page, size),
                            persistencePort.countAllActive()));
            });
    }

    private Mono<PagedResponse<Gama>> fetchFromDbAndCache(
            String key, int page, int size,
            reactor.core.publisher.Flux<Gama> dataFlux,
            Mono<Long> countMono) {
        return Mono.zip(dataFlux.collectList(), countMono)
            .map(tuple -> PagedResponse.of(tuple.getT1(), page + 1, size, tuple.getT2()))
            .flatMap(paged -> cachePort.savePaged(key, paged)
                .onErrorResume(e -> {
                    log.warn("Cache write error (key={}): {}", key, e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(paged));
    }

    @Override
    public Mono<Gama> update(Integer id, Gama gama) {
        normalizeName(gama);
        return persistencePort.findById(id)
            .switchIfEmpty(Mono.error(new GamaException("Gama no encontrada para actualizar", "NOT_FOUND")))
            .flatMap(existing -> {
                gama.setId(id);
                gama.setCreatedAt(existing.getCreatedAt());
                gama.setUpdatedAt(OffsetDateTime.now());
                if (gama.getStatus() == null) gama.setStatus(existing.getStatus());
                if (gama.getName() == null) gama.setName(existing.getName());
                if (gama.getOrder() == null) gama.setOrder(existing.getOrder());

                return validateUniqueNameExcludingId(gama.getName(), id)
                    .then(persistencePort.save(gama))
                    .flatMap(updated -> namespacePort.incrementVersion()
                        .thenReturn(updated));
            });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        return persistencePort.existsById(id)
            .flatMap(exists -> {
                if (!exists) return Mono.error(new GamaException("Gama no encontrada para eliminar", "NOT_FOUND"));
                return persistencePort.logicalDelete(id)
                    .then(namespacePort.incrementVersion())
                    .then();
            });
    }

    private Mono<Void> validateUniqueName(String name) {
        return persistencePort.existsByName(name)
            .flatMap(exists -> exists
                ? Mono.error(new GamaException("Ya existe una gama con ese nombre", "DUPLICATE_NAME"))
                : Mono.empty());
    }

    private Mono<Void> validateUniqueNameExcludingId(String name, Integer id) {
        return persistencePort.existsByNameExcludingId(name, id)
            .flatMap(exists -> exists
                ? Mono.error(new GamaException("Ya existe una gama con ese nombre", "DUPLICATE_NAME"))
                : Mono.empty());
    }

    private void normalizeName(Gama gama) {
        if (gama.getName() != null) {
            gama.setName(gama.getName().trim());
        }
    }
}
