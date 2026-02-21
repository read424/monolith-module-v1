package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.RegisterGuideNoRollsRequest;
import reactor.core.publisher.Mono;

public interface RegisterGuideNoRollsUseCase {
    Mono<Void> registerGuide(RegisterGuideNoRollsRequest request);
}
