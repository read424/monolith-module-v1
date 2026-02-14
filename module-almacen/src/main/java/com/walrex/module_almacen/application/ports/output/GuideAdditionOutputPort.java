package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.AddGuideRequest;
import com.walrex.module_almacen.domain.model.dto.AddGuideResponse;
import reactor.core.publisher.Mono;

public interface GuideAdditionOutputPort {
    Mono<AddGuideResponse> saveGuide(AddGuideRequest request);
}
