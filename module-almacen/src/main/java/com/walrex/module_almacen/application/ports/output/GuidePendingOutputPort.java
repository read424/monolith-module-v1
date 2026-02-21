package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuidePendingProjection;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface GuidePendingOutputPort {
    Flux<GuidePendingProjection> findPendingGuides(LocalDate date);
}
