package com.walrex.despacho.module_liquidaciones.application.ports.output;

import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface ReporteDespachoSalidaRepositoryPort {

    Flux<ReporteDespachoSalida> findByFechaRangeAndEntregado(LocalDate fechaStart, LocalDate fechaEnd, Integer entregado, Integer idCliente);
}
