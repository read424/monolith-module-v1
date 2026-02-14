package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.GuidePendingResponse;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface GuidePendingUseCase {
    Flux<GuidePendingResponse> getPendingGuides(LocalDate date);
}
