package com.walrex.despacho.module_liquidaciones.application.ports.input;

import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface GenerarReporteDespachoSalidaUseCase {

    Flux<ReporteDespachoSalida> generarReporte(LocalDate dateInit, LocalDate dateEnd, Integer entregado, Integer idCliente);
}
