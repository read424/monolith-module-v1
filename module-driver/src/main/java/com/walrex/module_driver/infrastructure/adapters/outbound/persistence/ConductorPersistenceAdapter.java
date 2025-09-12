package com.walrex.module_driver.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_driver.application.ports.output.ConductorPersistencePort;
import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;
import com.walrex.module_driver.domain.model.dto.SearchDriverByParameters;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.repository.ConductorBasicSearchRepository;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.repository.ConductorSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adaptador de persistencia para la búsqueda de conductores.
 * Implementa el patrón Adapter para separar la lógica de dominio de la persistencia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConductorPersistenceAdapter implements ConductorPersistencePort {

    private final ConductorBasicSearchRepository conductorBasicSearchRepository;
    private final ConductorSearchRepository conductorSearchRepository;

    @Override
    public Flux<ConductorDataDTO> buscarConductorPorDocumento(String numDoc, Integer idTipDoc) {
        log.info("🔍 Adapter: Delegando búsqueda básica al repository especializado");
        return conductorBasicSearchRepository.buscarConductorPorDocumento(numDoc, idTipDoc);
    }

    @Override
    public Flux<ConductorDataDTO> buscarConductorPorParametros(SearchDriverByParameters searchDriverByParameters) {
        log.info("🔍 Adapter: Delegando búsqueda dinámica al repository especializado");
        return conductorSearchRepository.buscarConductorPorParametros(searchDriverByParameters);
    }
}