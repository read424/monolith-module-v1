package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.RegisterGuideNoRollsRequest;
import reactor.core.publisher.Mono;

public interface RegisterGuideNoRollsOutputPort {
    Mono<Void> saveGuide(RegisterGuideNoRollsRequest request);
}
