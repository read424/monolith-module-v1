package com.walrex.module_comercial.domain.service;

import com.walrex.module_comercial.application.ports.input.GetOrdenProduccionPartidaUseCase;
import com.walrex.module_comercial.domain.dto.OrdenProduccionResponseDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.ComercialRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdenProduccionAssignateService implements GetOrdenProduccionPartidaUseCase {
    private final ComercialRepositoryAdapter comercialRepositoryAdapter;

    @Override
    public Mono<OrdenProduccionResponseDTO> getInfoOrdenProduccionPartida(Integer idPartida) {
        return null;
    }
}
