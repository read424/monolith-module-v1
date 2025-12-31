package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_revision_tela.application.ports.output.GuiaStatusPort;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.GuiaStatusProjection;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.GuiaStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adaptador de persistencia que implementa GuiaStatusPort
 * Responsable de obtener los status de guías desde la base de datos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuiaStatusPersistenceAdapter implements GuiaStatusPort {

    private final GuiaStatusRepository guiaStatusRepository;

    @Override
    public Flux<GuiaStatusProjection> obtenerGuiasConStatusRollos() {
        log.debug("Consultando guías con status de rollos desde la base de datos");

        return guiaStatusRepository.findGuiasConStatusRollos()
            .doOnNext(guia -> log.trace("Guía encontrada: id={}, status={}",
                guia.getIdOrdeningreso(), guia.getStatus()))
            .doOnComplete(() -> log.debug("Consulta de guías completada"))
            .doOnError(error -> log.error("Error consultando guías con status de rollos: {}",
                error.getMessage()));
    }
}
