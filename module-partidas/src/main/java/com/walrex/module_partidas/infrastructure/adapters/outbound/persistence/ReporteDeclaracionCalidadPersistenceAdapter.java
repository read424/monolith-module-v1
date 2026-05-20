package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.ReporteDeclaracionCalidadPort;
import com.walrex.module_partidas.domain.model.dto.ReporteDeclaracionCalidadDTO;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.ReporteDeclaracionCalidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ReporteDeclaracionCalidadPersistenceAdapter implements ReporteDeclaracionCalidadPort {

    private final ReporteDeclaracionCalidadRepository repository;

    @Override
    public Flux<ReporteDeclaracionCalidadDTO> findByFechaAndUbicacion(Integer idUbicacion, String fechaDeclaracion) {
        return repository.findByFechaAndUbicacion(idUbicacion, fechaDeclaracion);
    }
}
