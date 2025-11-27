package com.walrex.despacho.module_liquidaciones.application.ports.input;

import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface GenerarReporteDespachoSalidaUseCase {

    /**
     * Genera un reporte de despacho de salidas en formato de datos reactivos.
     *
     * @param dateInit Fecha de inicio del rango
     * @param dateEnd Fecha de fin del rango
     * @param isDespachado Filtro de estado de despacho (1=despachado, 0=no despachado)
     * @param idCliente ID del cliente (opcional)
     * @return Flujo reactivo con los datos del reporte
     */
    Flux<ReporteDespachoSalida> generarReporte(LocalDate dateInit, LocalDate dateEnd, Integer isDespachado, Integer idCliente);

    /**
     * Genera un reporte de despacho de salidas completo en formato Excel.
     * Este método coordina la obtención de datos y la generación del archivo Excel.
     *
     * @param dateInit Fecha de inicio del rango
     * @param dateEnd Fecha de fin del rango
     * @param isDespachado Filtro de estado de despacho (1=despachado, 0=no despachado)
     * @param idCliente ID del cliente (opcional)
     * @return Mono con los bytes del archivo Excel generado
     */
    Mono<byte[]> generarReporteExcel(LocalDate dateInit, LocalDate dateEnd, Integer isDespachado, Integer idCliente);
}
