package com.walrex.module_laboratorio.application.ports.input;

import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import reactor.core.publisher.Mono;

public interface GetCurvaDisenoByIdUseCase {
    Mono<CurvaDiseno> getById(Integer id);
}
