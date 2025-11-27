package com.walrex.despacho.module_liquidaciones.application.ports.output;

import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de salida para generación de reportes Excel.
 * Define el contrato que deben implementar los adaptadores de generación de reportes.
 */
public interface ExcelReportGeneratorPort {

    /**
     * Genera un reporte en formato Excel con los datos proporcionados.
     *
     * @param data Lista de datos del reporte de despacho de salida
     * @param startDate Fecha de inicio del rango del reporte
     * @param endDate Fecha de fin del rango del reporte
     * @return Array de bytes con el contenido del archivo Excel generado
     */
    byte[] generateReport(List<ReporteDespachoSalida> data, LocalDate startDate, LocalDate endDate);
}