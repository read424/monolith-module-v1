package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.ListPartidasPort;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaTypeaheadItem;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.PartidasTypeaheadRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ListPartidasPersistenceAdapter implements ListPartidasPort {

    private final PartidasTypeaheadRepository partidasTypeaheadRepository;

    @Override
    public Mono<PagedResponse<PartidaTypeaheadItem>> listPartidas(String search, int page, int size) {
        return partidasTypeaheadRepository.listPartidas(search, page, size);
    }
}
