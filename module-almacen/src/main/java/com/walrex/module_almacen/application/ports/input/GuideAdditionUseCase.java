package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.AddGuideRequest;
import com.walrex.module_almacen.domain.model.dto.AddGuideResponse;
import reactor.core.publisher.Mono;

public interface GuideAdditionUseCase {
    Mono<AddGuideResponse> addGuide(AddGuideRequest request);
}
