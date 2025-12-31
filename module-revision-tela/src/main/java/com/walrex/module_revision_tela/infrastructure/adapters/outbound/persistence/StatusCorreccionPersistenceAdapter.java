package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_revision_tela.application.ports.output.StatusCorreccionPort;
import com.walrex.module_revision_tela.domain.model.StatusCorreccion;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoCorregirStatusEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.StatusCorreccionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia que implementa StatusCorreccionPort
 * Responsable de guardar las correcciones de status en la base de datos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusCorreccionPersistenceAdapter implements StatusCorreccionPort {

    private final StatusCorreccionRepository statusCorreccionRepository;

    @Override
    public Mono<StatusCorreccion> guardarCorreccion(StatusCorreccion correccion) {
        log.debug("Guardando corrección de status para guía: {}", correccion.getIdOrdeningreso());

        IngresoCorregirStatusEntity entity = IngresoCorregirStatusEntity.builder()
            .idOrdeningreso(correccion.getIdOrdeningreso())
            .statusActual(correccion.getStatusActual())
            .statusNuevo(correccion.getStatusNuevo())
            .build();

        return statusCorreccionRepository.save(entity)
            .map(savedEntity -> StatusCorreccion.builder()
                .idOrdeningreso(savedEntity.getIdOrdeningreso())
                .statusActual(savedEntity.getStatusActual())
                .statusNuevo(savedEntity.getStatusNuevo())
                .build())
            .doOnSuccess(saved -> log.debug("Corrección guardada exitosamente para guía: {}",
                saved.getIdOrdeningreso()))
            .doOnError(error -> log.error("Error guardando corrección para guía {}: {}",
                correccion.getIdOrdeningreso(), error.getMessage()));
    }
}
