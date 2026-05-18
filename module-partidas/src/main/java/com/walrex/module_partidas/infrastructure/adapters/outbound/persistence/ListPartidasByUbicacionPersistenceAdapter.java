package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.ListPartidasByUbicacionPort;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaProduccion;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.PartidaUbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ListPartidasByUbicacionPersistenceAdapter implements ListPartidasByUbicacionPort {

    private final PartidaUbicacionRepository repository;

    @Override
    public Mono<PagedResponse<PartidaProduccion>> listByUbicacion(Integer idUbicacion, LocalDate fecha, String search, int page, int size) {
        return repository.findByUbicacion(idUbicacion, fecha, search, page, size);
    }
}
