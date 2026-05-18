package com.walrex.module_partidas.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_partidas.application.ports.input.ListPartidasUseCase;
import com.walrex.module_partidas.application.ports.output.ListPartidasPort;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaTypeaheadItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListPartidasService implements ListPartidasUseCase {

    private final ListPartidasPort listPartidasPort;

    @Override
    public Mono<PagedResponse<PartidaTypeaheadItem>> listPartidas(String search, int page, int size) {
        log.info("Listando partidas para typeahead - page={}, size={}, search={}", page, size, search);
        String normalizedSearch = search == null ? "" : search.trim();
        if (normalizedSearch.length() < 4) {
            return Mono.just(PagedResponse.of(java.util.List.of(), Math.max(page, 0), size <= 0 ? 10 : size, 0));
        }
        return listPartidasPort.listPartidas(normalizedSearch, page, size);
    }
}
