package com.walrex.module_laboratorio.application.ports.input;
import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import reactor.core.publisher.Mono;
public interface ListGamasUseCase {
    Mono<PagedResponse<Gama>> listAll(int page, int size);
}
