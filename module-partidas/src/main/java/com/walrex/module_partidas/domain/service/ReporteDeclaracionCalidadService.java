package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.ReporteDeclaracionCalidadUseCase;
import com.walrex.module_partidas.application.ports.output.ReporteDeclaracionCalidadPort;
import com.walrex.module_partidas.domain.model.dto.ReporteDeclaracionCalidadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteDeclaracionCalidadService implements ReporteDeclaracionCalidadUseCase {

    private final ReporteDeclaracionCalidadPort reportePort;

    @Override
    public Flux<ReporteDeclaracionCalidadDTO> obtenerReporte(Integer idUbicacion, String fechaDeclaracion) {
        log.info("Generando reporte declaracion calidad: fecha={}, idUbicacion={}", fechaDeclaracion, idUbicacion);
        return reportePort.findByFechaAndUbicacion(idUbicacion, fechaDeclaracion);
    }
}
