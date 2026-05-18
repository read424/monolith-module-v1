package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaListItem;
import reactor.core.publisher.Mono;

public interface ListPartidasSimpleUseCase {
    Mono<PagedResponse<PartidaListItem>> listPartidas(String search, int page, int size);
}
