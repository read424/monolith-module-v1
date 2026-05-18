package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.ListPartidasSimplePort;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaListItem;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.PartidaSimpleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ListPartidasSimplePersistenceAdapter implements ListPartidasSimplePort {

    private final PartidaSimpleRepository repository;

    @Override
    public Mono<PagedResponse<PartidaListItem>> listPartidas(String search, int page, int size) {
        return repository.listPartidas(search, page, size);
    }
}
