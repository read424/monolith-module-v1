package com.walrex.despacho.module_liquidaciones.domain.service;

import com.walrex.despacho.module_liquidaciones.application.ports.input.GenerarReporteDespachoSalidaUseCase;
import com.walrex.despacho.module_liquidaciones.application.ports.output.ReporteDespachoSalidaRepositoryPort;
import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerarReporteDespachoSalidaService implements GenerarReporteDespachoSalidaUseCase {

    private final ReporteDespachoSalidaRepositoryPort reporteDespachoSalidaRepositoryPort;

    @Override
    public Flux<ReporteDespachoSalida> generarReporte(LocalDate dateInit, LocalDate dateEnd, Integer entregado, Integer idCliente) {
        log.info("Generando reporte de despacho salidas desde {} hasta {}, entregado: {}, idCliente: {}", dateInit, dateEnd, entregado, idCliente);

        return reporteDespachoSalidaRepositoryPort.findByFechaRangeAndEntregado(dateInit, dateEnd, entregado, idCliente)
            .doOnComplete(() -> log.info("Reporte de despacho salidas generado exitosamente"))
            .doOnError(e -> log.error("Error generando reporte de despacho salidas: {}", e.getMessage()));
    }
}
