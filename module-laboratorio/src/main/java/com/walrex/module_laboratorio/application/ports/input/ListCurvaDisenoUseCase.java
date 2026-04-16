package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.domain.model.PagedResponse;
import reactor.core.publisher.Mono;

public interface ListCurvaDisenoUseCase {
    Mono<PagedResponse<CurvaDiseno>> listAll(String search, int page, int size);
}
