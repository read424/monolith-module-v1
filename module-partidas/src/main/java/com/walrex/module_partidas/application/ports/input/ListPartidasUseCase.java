package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaTypeaheadItem;

import reactor.core.publisher.Mono;

public interface ListPartidasUseCase {

    Mono<PagedResponse<PartidaTypeaheadItem>> listPartidas(String search, int page, int size);
}
