package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaListItem;
import reactor.core.publisher.Mono;

public interface ListPartidasSimplePort {
    Mono<PagedResponse<PartidaListItem>> listPartidas(String search, int page, int size);
}
