package com.walrex.module_machines.application.service;

import com.walrex.module_machines.application.ports.input.ListMaquinasUseCase;
import com.walrex.module_machines.application.ports.output.MaquinaCachePort;
import com.walrex.module_machines.application.ports.output.MaquinaPersistencePort;
import com.walrex.module_machines.domain.model.Maquina;
import com.walrex.module_machines.domain.model.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaquinaService implements ListMaquinasUseCase {

    private final MaquinaPersistencePort persistencePort;
    private final MaquinaCachePort cachePort;

    @Override
    public Mono<PagedResponse<Maquina>> listByUbicacion(Integer idUbicacion, String search, int page, int size) {
        return cachePort.getAll(idUbicacion)
                .onErrorResume(e -> {
                    log.warn("Cache read error (idUbicacion={}): {} — fallback a BD", idUbicacion, e.getMessage());
                    return Mono.empty();
                })
                .switchIfEmpty(fetchFromDbAndCache(idUbicacion))
                .map(all -> applyFilterAndPage(all, search, page, size));
    }

    private Mono<List<Maquina>> fetchFromDbAndCache(Integer idUbicacion) {
        return persistencePort.findAllByUbicacion(idUbicacion)
                .collectList()
                .flatMap(list -> cachePort.saveAll(idUbicacion, list)
                        .onErrorResume(e -> {
                            log.warn("Cache write error (idUbicacion={}): {}", idUbicacion, e.getMessage());
                            return Mono.empty();
                        })
                        .thenReturn(list));
    }

    private PagedResponse<Maquina> applyFilterAndPage(List<Maquina> all, String search, int page, int size) {
        String term = (search == null) ? "" : search.trim().toLowerCase();
        List<Maquina> filtered = term.isEmpty()
                ? all
                : all.stream()
                        .filter(m -> m.getDescMaq() != null && m.getDescMaq().toLowerCase().contains(term))
                        .toList();

        long total = filtered.size();
        int from = Math.min((page - 1) * size, (int) total);
        int to = Math.min(from + size, (int) total);
        List<Maquina> paged = filtered.subList(from, to);

        return PagedResponse.of(paged, page, size, total);
    }
}
