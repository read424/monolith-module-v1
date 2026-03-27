package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.GuidePendingOutputPort;
import com.walrex.module_almacen.domain.model.GuidePendingRecord;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.GuidePendingProjectionMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.GuidePendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuidePendingPersistenceAdapter implements GuidePendingOutputPort {

    private final GuidePendingRepository repository;
    private final GuidePendingProjectionMapper projectionMapper;

    @Override
    public Flux<GuidePendingRecord> findPendingGuides(LocalDate date) {
        return repository.findPendingGuides(date)
                .doOnSubscribe(subscription -> log.info("GuidePendingPersistenceAdapter.findPendingGuides date={}", date))
                .doOnNext(projection -> log.info(
                        "GuidePendingPersistenceAdapter projection ordenIngreso={}, detalle={}, peso_ref={}",
                        projection.getId_ordeningreso(),
                        projection.getId_detordeningreso(),
                        projection.getPeso_ref()))
                .map(projectionMapper::toDomain)
                .doOnNext(record -> log.info(
                        "GuidePendingPersistenceAdapter domain ordenIngreso={}, detalle={}, peso_ref={}",
                        record.getId_ordeningreso(),
                        record.getId_detordeningreso(),
                        record.getPeso_ref()));
    }
}
