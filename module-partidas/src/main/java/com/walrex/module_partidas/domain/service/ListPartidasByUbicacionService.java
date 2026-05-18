package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.ListPartidasByUbicacionUseCase;
import com.walrex.module_partidas.application.ports.output.ListPartidasByUbicacionPort;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaProduccion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ListPartidasByUbicacionService implements ListPartidasByUbicacionUseCase {

    private final ListPartidasByUbicacionPort listPartidasByUbicacionPort;

    @Override
    public Mono<PagedResponse<PartidaProduccion>> listByUbicacion(Integer idUbicacion, LocalDate fecha, String search, int page, int size) {
        return listPartidasByUbicacionPort.listByUbicacion(idUbicacion, fecha, search, page, size);
    }
}
