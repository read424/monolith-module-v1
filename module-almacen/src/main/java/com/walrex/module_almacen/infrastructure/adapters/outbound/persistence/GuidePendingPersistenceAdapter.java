package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.GuidePendingOutputPort;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuidePendingProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class GuidePendingPersistenceAdapter implements GuidePendingOutputPort {

    private final OrdenIngresoRepository repository;

    @Override
    public Flux<GuidePendingProjection> findPendingGuides(LocalDate date) {
        return repository.findPendingGuides(date);
    }
}
