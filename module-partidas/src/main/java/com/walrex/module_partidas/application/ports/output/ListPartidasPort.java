package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaTypeaheadItem;

import reactor.core.publisher.Mono;

public interface ListPartidasPort {

    Mono<PagedResponse<PartidaTypeaheadItem>> listPartidas(String search, int page, int size);
}
