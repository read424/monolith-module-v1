package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaProduccion;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ListPartidasByUbicacionUseCase {
    Mono<PagedResponse<PartidaProduccion>> listByUbicacion(Integer idUbicacion, LocalDate fecha, String search, int page, int size);
}
