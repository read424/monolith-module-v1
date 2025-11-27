package com.walrex.despacho.module_liquidaciones.infrastructure.adapters.outbound.report;

import com.walrex.despacho.module_liquidaciones.application.ports.output.ExcelReportGeneratorPort;
import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class DespachoSalidaExcelReportGenerator implements ExcelReportGeneratorPort {

    private static final String SHEET_NAME = "Reporte Despacho Salidas";
    private static final String REPORT_TITLE = "REPORTE DE DESPACHO DE SALIDAS";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateReport(List<ReporteDespachoSalida> data, LocalDate startDate, LocalDate endDate) {
        log.debug("Generando reporte Excel con {} registros", data.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            // Crear estilos
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle subtitleStyle = createSubtitleStyle(workbook);
            CellStyle dateEmisionStyle = createDateEmisionStyle(workbook);
            CellStyle summaryLabelStyle = createSummaryLabelStyle(workbook);
            CellStyle summaryValueStyle = createSummaryValueStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dataDateStyle = createDataDateStyle(workbook);
            CellStyle dataNumberStyle = createDataNumberStyle(workbook);
            CellStyle dataPercentStyle = createDataPercentStyle(workbook);

            int currentRow = 0;

            // Título y fecha de emisión
            currentRow = createHeader(sheet, titleStyle, subtitleStyle, dateEmisionStyle, startDate, endDate, currentRow);

            // Espacio
            currentRow++;

            // Resumen
            currentRow = createSummary(sheet, data, summaryLabelStyle, summaryValueStyle, workbook, currentRow);

            // Espacio antes de la tabla
            currentRow += 2;

            // Cabecera de tabla
            currentRow = createTableHeader(sheet, headerStyle, currentRow);

            // Datos
            createDataRows(sheet, data, dataStyle, dataDateStyle, dataNumberStyle, dataPercentStyle, currentRow);

            // Ajustar ancho de columnas
            adjustColumnWidths(sheet);

            workbook.write(outputStream);
            log.debug("Reporte Excel generado exitosamente");

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generando reporte Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando reporte Excel", e);
        }
    }

    private int createHeader(Sheet sheet, CellStyle titleStyle, CellStyle subtitleStyle,
                              CellStyle dateEmisionStyle, LocalDate startDate, LocalDate endDate, int rowNum) {
        // Fila del título
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(REPORT_TITLE);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 7));

        // Fecha de emisión
        Cell dateCell = titleRow.createCell(8);
        dateCell.setCellValue("Fecha emisión: " + LocalDate.now().format(DATE_FORMATTER));
        dateCell.setCellStyle(dateEmisionStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 8, 9));

        rowNum++;

        // Subtítulo con rango de fechas (solo si son diferentes)
        Row subtitleRow = sheet.createRow(rowNum);
        Cell subtitleCell = subtitleRow.createCell(0);

        String rangoFechas;
        if (startDate.equals(endDate)) {
            rangoFechas = "Fecha: " + startDate.format(DATE_FORMATTER);
        } else {
            rangoFechas = "Período: " + startDate.format(DATE_FORMATTER) + " al " + endDate.format(DATE_FORMATTER);
        }

        subtitleCell.setCellValue(rangoFechas);
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 9));

        return rowNum + 1;
    }

    private int createSummary(Sheet sheet, List<ReporteDespachoSalida> data,
                              CellStyle labelStyle, CellStyle valueStyle, Workbook workbook, int rowNum) {

        CellStyle summaryDataValueStyle = createSummaryDataValueStyle(workbook);

        // Calcular totales
        int totalRollos = data.stream()
            .mapToInt(r -> r.cntRollos() != null ? r.cntRollos() : 0)
            .sum();

        BigDecimal totalKgIngreso = data.stream()
            .map(r -> r.kgIngreso() != null ? r.kgIngreso() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long numLiquidaciones = data.stream()
            .map(ReporteDespachoSalida::idLiquidacion)
            .distinct()
            .count();

        long numPartidas = data.stream()
            .map(ReporteDespachoSalida::codPartida)
            .filter(cod -> cod != null && !cod.isEmpty())
            .distinct()
            .count();

        // Estilo para el título de la sección
        CellStyle sectionTitleStyle = createSectionTitleStyle(workbook);

        // Título de sección "RESUMEN"
        Row titleRow = sheet.createRow(rowNum);
        Cell sectionTitle = titleRow.createCell(0);
        sectionTitle.setCellValue("RESUMEN ESTADÍSTICO");
        sectionTitle.setCellStyle(sectionTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 9));
        rowNum++;

        // Fila 1: Total Rollos y Total KG
        Row row1 = sheet.createRow(rowNum);
        createSummaryCell(row1, 1, "Total de Rollos:", labelStyle);
        createSummaryCell(row1, 2, String.valueOf(totalRollos), summaryDataValueStyle);
        createSummaryCell(row1, 3, "Total Kg Ingreso:", labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 3, 4));
        createSummaryCell(row1, 5, String.format("%,.2f", totalKgIngreso), summaryDataValueStyle);

        // Fila 2: Num Liquidaciones y Num Partidas
        Row row2 = sheet.createRow(++rowNum);
        createSummaryCell(row2, 1, "N° Liquidaciones:", labelStyle);
        createSummaryCell(row2, 2, String.valueOf(numLiquidaciones), summaryDataValueStyle);
        createSummaryCell(row2, 3, "N° Partidas Despachadas:", labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 3, 4));
        createSummaryCell(row2, 5, String.valueOf(numPartidas), summaryDataValueStyle);

        return rowNum+1;
    }

    private void createSummaryCell(Row row, int colNum, String value, CellStyle style) {
        Cell cell = row.createCell(colNum);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private int createTableHeader(Sheet sheet, CellStyle headerStyle, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);

        String[] headers = {
            "N° Liquidación",
            "Razón Social",
            "Fecha Liquidación",
            "Entregado",
            "Partida",
            "Cant. Rollos",
            "N° Guía",
            "Kg Ingreso",
            "Kg Salida",
            "% Merma"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        return rowNum + 1;
    }

    private void createDataRows(Sheet sheet, List<ReporteDespachoSalida> data,
                                CellStyle dataStyle, CellStyle dateStyle,
                                CellStyle numberStyle, CellStyle percentStyle, int startRow) {
        int rowNum = startRow;

        for (ReporteDespachoSalida item : data) {
            Row row = sheet.createRow(rowNum++);

            // N° Liquidación
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(item.idLiquidacion() != null ? item.idLiquidacion() : 0);
            cell0.setCellStyle(dataStyle);

            // Razón Social
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(item.razon_social() != null ? item.razon_social() : "");
            cell1.setCellStyle(dataStyle);

            // Fecha Liquidación
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(item.fecLiquidacion() != null ? item.fecLiquidacion().format(DATE_FORMATTER) : "");
            cell2.setCellStyle(dateStyle);

            // Entregado
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(item.entregado() != null && item.entregado() == 1 ? "Sí" : "No");
            cell3.setCellStyle(dataStyle);

            // Partida
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(item.codPartida() != null ? item.codPartida() : "");
            cell4.setCellStyle(dataStyle);

            // Cant. Rollos
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(item.cntRollos() != null ? item.cntRollos() : 0);
            cell5.setCellStyle(numberStyle);

            // N° Guía
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(item.numGuia() != null ? item.numGuia() : "");
            cell6.setCellStyle(dataStyle);

            // Kg Ingreso
            Cell cell7 = row.createCell(7);
            if (item.kgIngreso() != null) {
                cell7.setCellValue(item.kgIngreso().doubleValue());
            }
            cell7.setCellStyle(numberStyle);

            // Kg Salida
            Cell cell8 = row.createCell(8);
            if (item.kgSalida() != null) {
                cell8.setCellValue(item.kgSalida().doubleValue());
            }
            cell8.setCellStyle(numberStyle);

            // % Merma
            Cell cell9 = row.createCell(9);
            if (item.porcMerma() != null) {
                cell9.setCellValue(item.porcMerma().doubleValue());
            }
            cell9.setCellStyle(percentStyle);
        }
    }

    private void adjustColumnWidths(Sheet sheet) {
        int[] columnWidths = {4000, 8000, 4500, 3000, 4000, 3500, 4000, 4000, 4000, 3000};
        for (int i = 0; i < columnWidths.length; i++) {
            sheet.setColumnWidth(i, columnWidths[i]);
        }
    }

    // Estilos

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSectionTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateEmisionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSummaryLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createSummaryValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createSummaryDataValueStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Color de fondo azul oscuro empresarial
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Fuente blanca y negrita
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        // Alineación y bordes
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        return style;
    }

    private CellStyle createDataDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createDataPercentStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }
}
