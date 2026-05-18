package com.walrex.module_machines.application.ports.input;

import com.walrex.module_machines.domain.model.Maquina;
import com.walrex.module_machines.domain.model.PagedResponse;
import reactor.core.publisher.Mono;

public interface ListMaquinasUseCase {
    Mono<PagedResponse<Maquina>> listByUbicacion(Integer idUbicacion, String search, int page, int size);
}
