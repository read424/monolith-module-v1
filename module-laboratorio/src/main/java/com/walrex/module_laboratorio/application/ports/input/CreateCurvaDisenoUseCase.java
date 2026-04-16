package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import reactor.core.publisher.Mono;

public interface CreateCurvaDisenoUseCase {
    Mono<CurvaDiseno> create(CurvaDiseno curvaDiseno, String idempotencyKey);
}
