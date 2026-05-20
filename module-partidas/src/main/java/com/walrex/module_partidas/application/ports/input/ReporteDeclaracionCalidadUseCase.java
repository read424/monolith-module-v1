package com.walrex.module_partidas.application.ports.input;

import com.walrex.module_partidas.domain.model.dto.ReporteDeclaracionCalidadDTO;
import reactor.core.publisher.Flux;

public interface ReporteDeclaracionCalidadUseCase {
    Flux<ReporteDeclaracionCalidadDTO> obtenerReporte(Integer idUbicacion, String fechaDeclaracion);
}
