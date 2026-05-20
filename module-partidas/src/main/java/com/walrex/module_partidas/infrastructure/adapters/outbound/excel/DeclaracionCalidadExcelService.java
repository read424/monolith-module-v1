package com.walrex.module_partidas.infrastructure.adapters.outbound.excel;

import com.walrex.module_partidas.domain.model.dto.ReporteDeclaracionCalidadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclaracionCalidadExcelService {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] HEADERS = {
        "CLIENTE", "F. PROGRAMA", "F. TEÑIDO", "F. AUDITADO",
        "AREA", "COD. PARTIDA", "ARTÍCULO", "COLOR",
        "ROLLOS", "NIVEL RECHAZO", "MOTIVO RECHAZO", "OBSERVACIÓN", "DÍAS"
    };

    private static final int[] ANCHOS = {30, 12, 12, 12, 12, 14, 35, 20, 8, 12, 30, 35, 8};

    public Flux<DataBuffer> generarExcel(List<ReporteDeclaracionCalidadDTO> registros,
                                          String fechaDeclaracion,
                                          DataBufferFactory bufferFactory) {
        return Mono.fromCallable(() -> {
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Declaraciones Calidad");
                configurarColumnas(sheet);

                int fila = crearCabecera(sheet, workbook, fechaDeclaracion);
                fila = crearHeaders(sheet, workbook, fila);
                llenarDatos(sheet, workbook, registros, fila);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                log.info("Excel generado: {} registros, {} bytes", registros.size(), out.size());
                return bufferFactory.wrap(out.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Error generando reporte Excel declaraciones calidad", e);
            }
        }).flux();
    }

    private void configurarColumnas(Sheet sheet) {
        for (int i = 0; i < ANCHOS.length; i++) {
            sheet.setColumnWidth(i, ANCHOS[i] * 256);
        }
    }

    private int crearCabecera(Sheet sheet, Workbook workbook, String fechaDeclaracion) {
        int ultimo = HEADERS.length - 1;

        Row r0 = sheet.createRow(0);
        crearCeldaMerge(sheet, r0, 0, ultimo, "TEXTIL LA MERCED S.A.C.",
                estiloTexto(workbook, (short) 14, true, HorizontalAlignment.CENTER, null));

        Row r1 = sheet.createRow(1);
        crearCeldaMerge(sheet, r1, 0, ultimo, "REPORTE DECLARACIONES DE CALIDAD",
                estiloTexto(workbook, (short) 12, true, HorizontalAlignment.CENTER, null));

        Row r2 = sheet.createRow(2);
        crearCeldaMerge(sheet, r2, 0, ultimo, "FECHA: " + fechaDeclaracion,
                estiloTexto(workbook, (short) 10, false, HorizontalAlignment.CENTER, null));

        Row r3 = sheet.createRow(3);
        String generado = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        crearCeldaMerge(sheet, r3, 0, ultimo, "GENERADO: " + generado,
                estiloTexto(workbook, (short) 9, false, HorizontalAlignment.CENTER, null));

        sheet.createRow(4);
        return 5;
    }

    private int crearHeaders(Sheet sheet, Workbook workbook, int fila) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);

        Row row = sheet.createRow(fila);
        row.setHeightInPoints(30);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(style);
        }
        return fila + 1;
    }

    private void llenarDatos(Sheet sheet, Workbook workbook,
                              List<ReporteDeclaracionCalidadDTO> registros, int filaInicio) {
        CellStyle estFecha   = estiloDato(workbook, HorizontalAlignment.CENTER, false);
        CellStyle estTexto   = estiloDato(workbook, HorizontalAlignment.LEFT,   false);
        CellStyle estCentro  = estiloDato(workbook, HorizontalAlignment.CENTER, false);
        CellStyle estObs     = estiloDato(workbook, HorizontalAlignment.LEFT,   true);
        CellStyle estNumero  = estiloDato(workbook, HorizontalAlignment.RIGHT,  false);
        CellStyle estDias    = estiloDato(workbook, HorizontalAlignment.CENTER, false);

        // estilo para dias con color según antigüedad
        CellStyle estDiasAlerta = workbook.createCellStyle();
        estDiasAlerta.cloneStyleFrom(estDias);
        Font fuenteAlerta = workbook.createFont();
        fuenteAlerta.setBold(true);
        fuenteAlerta.setColor(IndexedColors.RED.getIndex());
        fuenteAlerta.setFontHeightInPoints((short) 9);
        estDiasAlerta.setFont(fuenteAlerta);

        int fila = filaInicio;
        LocalDate hoy = LocalDate.now();

        for (ReporteDeclaracionCalidadDTO r : registros) {
            Row row = sheet.createRow(fila++);

            long dias = (r.getFecIngreso() != null)
                    ? ChronoUnit.DAYS.between(r.getFecIngreso(), hoy) : 0L;

            setStr(row, 0,  r.getRazonSocial(),        estTexto);
            setStr(row, 1,  fmt(r.getFecProgramacion()), estFecha);
            setStr(row, 2,  fmt(r.getFecRealInicio()),   estFecha);
            setStr(row, 3,  fmt(r.getFechaDeclaracion()), estFecha);
            setStr(row, 4,  r.getTipoDeclaracion(),     estCentro);
            setStr(row, 5,  r.getCodPartida(),          estTexto);
            setStr(row, 6,  r.getDescArticulo(),        estTexto);
            setStr(row, 7,  r.getNoColor(),             estTexto);
            setNum(row, 8,  r.getCntRollos(),           estNumero);
            setStr(row, 9,  nivelCriticoLabel(r.getNivelCritico()), estCentro);
            setStr(row, 10, r.getDescMotivoRechazo(),   estObs);
            setStr(row, 11, r.getObservacion(),         estObs);
            setCellLong(row, 12, dias, dias > 30 ? estDiasAlerta : estDias);
        }
    }

    // --- helpers ---

    private String fmt(LocalDate d) {
        return d != null ? d.format(FMT_FECHA) : "";
    }

    private String nivelCriticoLabel(Integer nivel) {
        if (nivel == null) return "";
        return switch (nivel) {
            case 0  -> "Sin rechazo";
            case 1  -> "Nivel 1";
            case 2  -> "Nivel 2";
            case 3  -> "Nivel 3";
            default -> String.valueOf(nivel);
        };
    }

    private void crearCeldaMerge(Sheet sheet, Row row, int c1, int c2, String valor, CellStyle style) {
        Cell cell = row.createCell(c1);
        cell.setCellValue(valor);
        cell.setCellStyle(style);
        if (c2 > c1) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), c1, c2));
        }
    }

    private void setStr(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setNum(Row row, int col, Integer value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setCellLong(Row row, int col, long value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle estiloTexto(Workbook wb, short size, boolean bold,
                                   HorizontalAlignment align, Short bgColor) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints(size);
        style.setFont(font);
        style.setAlignment(align);
        if (bgColor != null) {
            style.setFillForegroundColor(bgColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private CellStyle estiloDato(Workbook wb, HorizontalAlignment align, boolean wrap) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(wrap);
        return style;
    }
}
