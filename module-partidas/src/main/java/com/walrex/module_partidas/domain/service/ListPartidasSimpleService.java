package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.ListPartidasSimpleUseCase;
import com.walrex.module_partidas.application.ports.output.ListPartidasSimplePort;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ListPartidasSimpleService implements ListPartidasSimpleUseCase {

    private final ListPartidasSimplePort listPartidasSimplePort;

    @Override
    public Mono<PagedResponse<PartidaListItem>> listPartidas(String search, int page, int size) {
        return listPartidasSimplePort.listPartidas(search, page, size);
    }
}
