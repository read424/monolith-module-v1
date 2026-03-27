package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.GuidePendingRecord;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface GuidePendingOutputPort {
    Flux<GuidePendingRecord> findPendingGuides(LocalDate date);
}
