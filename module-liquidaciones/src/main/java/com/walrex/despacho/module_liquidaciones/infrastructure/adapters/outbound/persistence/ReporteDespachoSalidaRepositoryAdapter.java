package com.walrex.despacho.module_liquidaciones.infrastructure.adapters.outbound.persistence;

import com.walrex.despacho.module_liquidaciones.application.ports.output.ReporteDespachoSalidaRepositoryPort;
import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import com.walrex.despacho.module_liquidaciones.infrastructure.adapters.outbound.persistence.repository.ReporteDespachoSalidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteDespachoSalidaRepositoryAdapter implements ReporteDespachoSalidaRepositoryPort {

    private final ReporteDespachoSalidaRepository reporteDespachoSalidaRepository;

    @Override
    public Flux<ReporteDespachoSalida> findByFechaRangeAndEntregado(LocalDate fechaStart, LocalDate fechaEnd, Integer entregado, Integer idCliente) {
        return reporteDespachoSalidaRepository.findByFechaRangeAndEntregado(fechaStart, fechaEnd, entregado, idCliente);
    }
}
