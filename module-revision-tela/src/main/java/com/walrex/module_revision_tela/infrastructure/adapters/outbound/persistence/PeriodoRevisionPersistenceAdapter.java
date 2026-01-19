package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_revision_tela.application.ports.output.PeriodoRevisionPort;
import com.walrex.module_revision_tela.domain.model.PeriodoRevision;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.PeriodoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.PeriodoRevisionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para PeriodoRevision
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodoRevisionPersistenceAdapter implements PeriodoRevisionPort {

    private final PeriodoRevisionRepository repository;

    @Override
    public Mono<PeriodoRevision> obtenerPeriodoActivo() {
        log.debug("Consultando periodo activo");
        return repository.findActivePeriodo()
            .map(this::toDomain)
            .doOnSuccess(periodo -> log.info("Periodo activo encontrado: id={}", periodo.getIdPeriodo()))
            .doOnError(error -> log.error("Error consultando periodo activo: {}", error.getMessage()));
    }

    /**
     * Convierte PeriodoRevisionEntity a PeriodoRevision (dominio)
     */
    private PeriodoRevision toDomain(PeriodoRevisionEntity entity) {
        return PeriodoRevision.builder()
            .idPeriodo(entity.getIdPeriodo())
            .monthPeriodo(entity.getMonthPeriodo())
            .anioPeriodo(entity.getAnioPeriodo())
            .status(entity.getStatus())
            .build();
    }
}
