package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.domain.model.dto.ReporteDeclaracionCalidadDTO;
import reactor.core.publisher.Flux;

public interface ReporteDeclaracionCalidadPort {
    Flux<ReporteDeclaracionCalidadDTO> findByFechaAndUbicacion(Integer idUbicacion, String fechaDeclaracion);
}
