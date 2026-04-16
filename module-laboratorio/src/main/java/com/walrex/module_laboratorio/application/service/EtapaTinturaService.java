package com.walrex.module_laboratorio.application.service;

import com.walrex.module_laboratorio.application.ports.input.EtapaTinturaUseCase;
import com.walrex.module_laboratorio.application.ports.output.EtapaTinturaCachePort;
import com.walrex.module_laboratorio.application.ports.output.EtapaTinturaPersistencePort;
import com.walrex.module_laboratorio.domain.exceptions.EtapaException;
import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtapaTinturaService implements EtapaTinturaUseCase {

    private final EtapaTinturaPersistencePort persistencePort;
    private final EtapaTinturaCachePort cachePort;

    private static final String CACHE_PREFIX = "etapa_tintura:";

    @Override
    public Mono<EtapaTintura> create(EtapaTintura etapa) {
        etapa.setFec_registro(LocalDate.now());
        etapa.setStatus(1); // Activo por defecto
        return persistencePort.save(etapa)
                .flatMap(saved -> cachePort.invalidate(CACHE_PREFIX + "*")
                        .thenReturn(saved))
                .doOnSuccess(s -> log.info("EtapaTintura creada con ID: {}", s.getId_tintura()));
    }

    @Override
    public Mono<EtapaTintura> findById(Integer id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new EtapaException("Etapa de tintura no encontrada", "NOT_FOUND")));
    }

    @Override
    public Flux<EtapaTintura> findAll(int page, int size) {
        String cacheKey = CACHE_PREFIX + "list_page_" + page + "_size_" + size;
        return cachePort.findAll(cacheKey)
                .switchIfEmpty(persistencePort.findAll(page, size)
                        .collectList()
                        .flatMap(list -> cachePort.saveAll(cacheKey, list).thenReturn(list))
                        .flatMapMany(Flux::fromIterable));
    }

    @Override
    public Mono<EtapaTintura> update(Integer id, EtapaTintura etapa) {
        return persistencePort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new EtapaException("Etapa de tintura no encontrada para actualizar", "NOT_FOUND"));
                    }
                    etapa.setId_tintura(id);
                    return persistencePort.save(etapa)
                            .flatMap(updated -> cachePort.invalidate(CACHE_PREFIX + "*")
                                    .thenReturn(updated));
                });
    }

    @Override
    public Mono<Void> delete(Integer id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new EtapaException("Etapa de tintura no encontrada para eliminar", "NOT_FOUND")))
                .flatMap(etapa -> {
                    etapa.setStatus(0); // Borrado lógico
                    return persistencePort.save(etapa)
                            .flatMap(deleted -> cachePort.invalidate(CACHE_PREFIX + "*"))
                            .then();
                });
    }
}
