package com.walrex.module_almacen.infrastructure.adapters.outbound.excel;

import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleDTO;
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
import java.util.List;

/**
 * 📊 SERVICIO SIMPLE DE EXPORTACIÓN EXCEL PARA KARDEX
 * Genera reportes Excel simples y claros con:
 * - Cabecera con información de empresa y fechas
 * - Estructura: FECHA | DETALLE | CANTIDAD | STOCK ANTES | STOCK ACTUAL | PRECIO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KardexExcelService {

    public Mono<Flux<DataBuffer>> generarExcelKardex(
        List<KardexArticuloDTO> articulos, 
        String fechaInicio, 
        String fechaFin,
        DataBufferFactory dataBufferFactory) {
        
        return Mono.fromCallable(() -> {
            try {
                log.info("🔄 Generando Excel simple para {} artículos", articulos.size());
                
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Reporte Kardex");
                
                // Crear cabecera genérica (SIN producto específico)
                int currentRow = crearCabeceraGenerica(sheet, fechaInicio, fechaFin);
                configurarColumnasSimples(sheet);
                
                // 📊 CREAR RESUMEN DE TOTALES CONSOLIDADO
                currentRow = crearResumenTotales(sheet, articulos, currentRow);
                
                // Llenar datos AGRUPADOS POR PRODUCTO
                for (KardexArticuloDTO articulo : articulos) {
                    currentRow = crearSeccionProducto(sheet, articulo, currentRow);
                }
                
                // Convertir a bytes
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                byte[] excelBytes = outputStream.toByteArray();
                
                log.info("✅ Excel simple generado exitosamente. Tamaño: {} bytes", excelBytes.length);
                
                DataBuffer dataBuffer = dataBufferFactory.wrap(excelBytes);
                return Flux.just(dataBuffer);
                
            } catch (IOException e) {
                log.error("❌ Error generando Excel: {}", e.getMessage(), e);
                throw new RuntimeException("Error generando reporte Excel", e);
            }
        });
    }
    
    /**
     * 📋 CREAR CABECERA GENÉRICA CON INFORMACIÓN DE EMPRESA
     */
    private int crearCabeceraGenerica(Sheet sheet, String fechaInicio, String fechaFin) {
        int currentRow = 0;
        
        // 🏢 EMPRESA
        Row empresaRow = sheet.createRow(currentRow++);
        Cell empresaCell = empresaRow.createCell(0);
        empresaCell.setCellValue("TEXTIL LA MERCED S.A.C.");
        
        CellStyle empresaStyle = sheet.getWorkbook().createCellStyle();
        Font empresaFont = sheet.getWorkbook().createFont();
        empresaFont.setBold(true);
        empresaFont.setFontHeightInPoints((short) 14);
        empresaStyle.setFont(empresaFont);
        empresaStyle.setAlignment(HorizontalAlignment.CENTER);
        empresaCell.setCellStyle(empresaStyle);
        
        // Unir celdas para centrar el título
        sheet.addMergedRegion(new CellRangeAddress(currentRow-1, currentRow-1, 0, 5));
        
        // 📊 TÍTULO
        Row tituloRow = sheet.createRow(currentRow++);
        Cell tituloCell = tituloRow.createCell(0);
        tituloCell.setCellValue("REPORTE KARDEX");
        
        CellStyle tituloStyle = sheet.getWorkbook().createCellStyle();
        Font tituloFont = sheet.getWorkbook().createFont();
        tituloFont.setBold(true);
        tituloFont.setFontHeightInPoints((short) 11);
        tituloStyle.setFont(tituloFont);
        tituloStyle.setAlignment(HorizontalAlignment.CENTER);
        tituloCell.setCellStyle(tituloStyle);
        
        sheet.addMergedRegion(new CellRangeAddress(currentRow-1, currentRow-1, 0, 5));
        
        // 📅 RANGO DE FECHAS
        Row fechasRow = sheet.createRow(currentRow++);
        Cell fechasCell = fechasRow.createCell(0);
        fechasCell.setCellValue("DESDE: " + formatearFecha(fechaInicio) + " HASTA: " + formatearFecha(fechaFin));
        
        CellStyle fechasStyle = sheet.getWorkbook().createCellStyle();
        Font fechasFont = sheet.getWorkbook().createFont();
        fechasFont.setFontHeightInPoints((short) 10);
        fechasStyle.setFont(fechasFont);
        fechasStyle.setAlignment(HorizontalAlignment.CENTER);
        fechasCell.setCellStyle(fechasStyle);
        
        sheet.addMergedRegion(new CellRangeAddress(currentRow-1, currentRow-1, 0, 5));
        
        // 🕒 FECHA DE GENERACIÓN
        Row generacionRow = sheet.createRow(currentRow++);
        Cell generacionCell = generacionRow.createCell(0);
        String fechaGeneracion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        generacionCell.setCellValue("GENERADO: " + fechaGeneracion);
        
        CellStyle generacionStyle = sheet.getWorkbook().createCellStyle();
        Font generacionFont = sheet.getWorkbook().createFont();
        generacionFont.setFontHeightInPoints((short) 9);
        generacionFont.setItalic(true);
        generacionStyle.setFont(generacionFont);
        generacionStyle.setAlignment(HorizontalAlignment.CENTER);
        generacionCell.setCellStyle(generacionStyle);
        
        sheet.addMergedRegion(new CellRangeAddress(currentRow-1, currentRow-1, 0, 5));
        
        // LÍNEA EN BLANCO
        sheet.createRow(currentRow++);
        
        return currentRow;
    }
    
    /**
     * 📏 CONFIGURAR ANCHOS DE COLUMNAS SIMPLES
     */
    private void configurarColumnasSimples(Sheet sheet) {
        sheet.setColumnWidth(0, 12 * 256);  // FECHA
        sheet.setColumnWidth(1, 50 * 256);  // DETALLE (más ancho)
        sheet.setColumnWidth(2, 18 * 256);  // STOCK ANTES
        sheet.setColumnWidth(3, 15 * 256);  // CANTIDAD
        sheet.setColumnWidth(4, 18 * 256);  // STOCK ACTUAL
        sheet.setColumnWidth(5, 15 * 256);  // PRECIO
    }
    
    /**
     * 📊 CREAR RESUMEN DE TOTALES CONSOLIDADO
     */
    private int crearResumenTotales(Sheet sheet, List<KardexArticuloDTO> articulos, int currentRow) {
        // 📊 CALCULAR TOTALES
        double totalValorizado = 0.0;
        int totalProductos = articulos.size();
        int totalMovimientos = 0;
        
        for (KardexArticuloDTO articulo : articulos) {
            // Sumar total valorizado
            if (articulo.getTotalValorizado() != null) {
                totalValorizado += articulo.getTotalValorizado().doubleValue();
            }
            // Contar movimientos
            if (articulo.getDetalles() != null) {
                totalMovimientos += articulo.getDetalles().size();
            }
        }
        
        // 📋 CREAR TÍTULO DE RESUMEN
        Row tituloResumenRow = sheet.createRow(currentRow++);
        tituloResumenRow.setHeightInPoints(25); // 🔧 AJUSTAR ALTURA DE LA FILA
        Cell tituloResumenCell = tituloResumenRow.createCell(0);
        tituloResumenCell.setCellValue("📈 RESUMEN CONSOLIDADO");
        
        CellStyle tituloResumenStyle = sheet.getWorkbook().createCellStyle();
        Font tituloResumenFont = sheet.getWorkbook().createFont();
        tituloResumenFont.setBold(true);
        tituloResumenFont.setFontHeightInPoints((short) 12);
        tituloResumenStyle.setFont(tituloResumenFont);
        tituloResumenStyle.setAlignment(HorizontalAlignment.CENTER);
        tituloResumenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        tituloResumenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tituloResumenCell.setCellStyle(tituloResumenStyle);
        
        // Unir celdas para el título
        sheet.addMergedRegion(new CellRangeAddress(currentRow-1, currentRow-1, 0, 5));
        
        // 💰 TOTAL VALORIZADO
        Row totalValorizadoRow = sheet.createRow(currentRow++);
        totalValorizadoRow.setHeightInPoints(22); // 🔧 AJUSTAR ALTURA DE LA FILA
        Cell totalValorizadoCell = totalValorizadoRow.createCell(0);
        totalValorizadoCell.setCellValue("💰 Total Valorizado:");
        
        Cell valorValorizadoCell = totalValorizadoRow.createCell(2);
        valorValorizadoCell.setCellValue(totalValorizado);
        
        // Estilo para valores monetarios (con decimales)
        CellStyle valorMonetarioStyle = crearEstiloValorResumen(sheet.getWorkbook());
        valorValorizadoCell.setCellStyle(valorMonetarioStyle);
        
        // 📦 TOTAL PRODUCTOS
        Row totalProductosRow = sheet.createRow(currentRow++);
        totalProductosRow.setHeightInPoints(22); // 🔧 AJUSTAR ALTURA DE LA FILA
        Cell totalProductosCell = totalProductosRow.createCell(0);
        totalProductosCell.setCellValue("📦 Productos Analizados:");
        
        Cell valorProductosCell = totalProductosRow.createCell(2);
        valorProductosCell.setCellValue(totalProductos);
        // 📊 Estilo para números enteros (sin decimales)
        CellStyle valorEnteroStyle = crearEstiloValorEntero(sheet.getWorkbook());
        valorProductosCell.setCellStyle(valorEnteroStyle);
        
        // 🔄 TOTAL MOVIMIENTOS
        Row totalMovimientosRow = sheet.createRow(currentRow++);
        totalMovimientosRow.setHeightInPoints(22); // 🔧 AJUSTAR ALTURA DE LA FILA
        Cell totalMovimientosCell = totalMovimientosRow.createCell(0);
        totalMovimientosCell.setCellValue("🔄 Movimientos Totales:");
        
        Cell valorMovimientosCell = totalMovimientosRow.createCell(2);
        valorMovimientosCell.setCellValue(totalMovimientos);
        // 📊 Estilo para números enteros (sin decimales)
        valorMovimientosCell.setCellStyle(valorEnteroStyle);
        
        // LÍNEA EN BLANCO DE SEPARACIÓN
        sheet.createRow(currentRow++);
        sheet.createRow(currentRow++); // Doble separación
        
        return currentRow;
    }
    
    /**
     * 📋 CREAR HEADERS DE TABLA SIMPLE
     */
    private int crearHeadersTabla(Sheet sheet, int currentRow) {
        Row headerRow = sheet.createRow(currentRow++);
        
        String[] headers = {"FECHA", "DETALLE", "STOCK ANTES (gr)", "CANTIDAD (gr)", "STOCK ACTUAL (gr)", "PRECIO"};
        
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        return currentRow;
    }
    
    /**
     * 📦 CREAR SECCIÓN COMPLETA DE UN PRODUCTO
     */
    private int crearSeccionProducto(Sheet sheet, KardexArticuloDTO articulo, int currentRow) {
        // 📦 SUBCABECERA DEL PRODUCTO
        currentRow = crearSubcabeceraProducto(sheet, articulo, currentRow);
        
        // 📋 HEADERS DE TABLA PARA ESTE PRODUCTO
        currentRow = crearHeadersTabla(sheet, currentRow);
        
        // 📊 DATOS DEL PRODUCTO
        currentRow = llenarDatosProducto(sheet, articulo, currentRow);
        
        // 📏 SEPARACIÓN ENTRE PRODUCTOS
        sheet.createRow(currentRow++); // Línea en blanco
        
        return currentRow;
    }
    
    /**
     * 📦 CREAR SUBCABECERA PARA UN PRODUCTO ESPECÍFICO
     */
    private int crearSubcabeceraProducto(Sheet sheet, KardexArticuloDTO articulo, int currentRow) {
        Row productoRow = sheet.createRow(currentRow++);
        Cell productoCell = productoRow.createCell(0);
        
        String nombreProducto = articulo.getCodArticulo() + " - " + articulo.getDescArticulo();
        productoCell.setCellValue("PRODUCTO: " + nombreProducto.toUpperCase());
        
        // Estilo para subcabecera del producto
        CellStyle productoStyle = sheet.getWorkbook().createCellStyle();
        Font productoFont = sheet.getWorkbook().createFont();
        productoFont.setBold(true);
        productoFont.setFontHeightInPoints((short) 12);
        productoStyle.setFont(productoFont);
        productoStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        productoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        productoCell.setCellStyle(productoStyle);
        
        // Unir celdas para que el nombre del producto se extienda
        sheet.addMergedRegion(new CellRangeAddress(currentRow-1, currentRow-1, 0, 5));
        
        return currentRow;
    }
    
    /**
     * 📊 LLENAR DATOS ESPECÍFICOS DE UN PRODUCTO
     */
    private int llenarDatosProducto(Sheet sheet, KardexArticuloDTO articulo, int currentRow) {
        // Crear estilos para los datos
        CellStyle fechaStyle = crearEstiloFecha(sheet.getWorkbook());
        CellStyle textoStyle = crearEstiloTexto(sheet.getWorkbook());
        CellStyle numeroStyle = crearEstiloNumero(sheet.getWorkbook());
        CellStyle monedaStyle = crearEstiloMoneda(sheet.getWorkbook());
        
        // 🎨 CREAR ESTILOS CON COLORES PARA INGRESOS Y SALIDAS
        CellStyle stockAntesBoldStyle = crearEstiloBoldNumero(sheet.getWorkbook());
        CellStyle cantidadIngresoBoldStyle = crearEstiloCantidadIngreso(sheet.getWorkbook()); // Azul oscuro
        CellStyle cantidadSalidaBoldStyle = crearEstiloCantidadSalida(sheet.getWorkbook());   // Rojo
        CellStyle stockActualBoldStyle = crearEstiloBoldNumero(sheet.getWorkbook());
        
        // Procesar detalles del artículo
        for (KardexDetalleDTO detalle : articulo.getDetalles()) {
            Row dataRow = sheet.createRow(currentRow++);
            
            // FECHA
            Cell fechaCell = dataRow.createCell(0);
            fechaCell.setCellValue(formatearFecha(detalle.getFec_movimiento().toString()));
            fechaCell.setCellStyle(fechaStyle);
            
            // DETALLE
            Cell detalleCell = dataRow.createCell(1);
            String descripcionDetalle = construirDetalle(detalle);
            detalleCell.setCellValue(descripcionDetalle);
            detalleCell.setCellStyle(textoStyle);
            
            // STOCK ANTES (posición 2 - REORDENADO)
            Cell stockAntesCell = dataRow.createCell(2);
            double stockActualConvertido = detalle.getStock_actual() != null ? detalle.getStock_actual().doubleValue() : 0.0;
            double cantidadMostrar = detalle.getCantidad() != null ? detalle.getCantidad().doubleValue() : 0.0;
            if (detalle.getType_kardex() == 1) { // INGRESO: convertir kg a gr
                stockActualConvertido = stockActualConvertido * 1000;
                cantidadMostrar = cantidadMostrar * 1000;
            }
            double stockAntes = stockActualConvertido - cantidadMostrar;
            stockAntesCell.setCellValue(stockAntes);
            stockAntesCell.setCellStyle(stockAntesBoldStyle);
            
            // CANTIDAD (posición 3 - REORDENADO con colores)
            Cell cantidadCell = dataRow.createCell(3);
            cantidadCell.setCellValue(cantidadMostrar);
            if (detalle.getType_kardex() == 1) { // INGRESO: azul oscuro
                cantidadCell.setCellStyle(cantidadIngresoBoldStyle);
            } else { // SALIDA: rojo
                cantidadCell.setCellStyle(cantidadSalidaBoldStyle);
            }
            
            // STOCK ACTUAL (posición 4)
            Cell stockActualCell = dataRow.createCell(4);
            stockActualCell.setCellValue(stockActualConvertido);
            stockActualCell.setCellStyle(stockActualBoldStyle);
            
            // PRECIO
            Cell precioCell = dataRow.createCell(5);
            double precio = detalle.getPrecio_compra() != null ? detalle.getPrecio_compra().doubleValue() : 0.0;
            precioCell.setCellValue(precio);
            precioCell.setCellStyle(monedaStyle);
        }
        
        return currentRow;
    }
    
    /**
     * 🏗️ MÉTODOS AUXILIARES PARA ESTILOS
     */
    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle crearEstiloTexto(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        return style;
    }
    
    private CellStyle crearEstiloNumero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.000"));
        return style;
    }
    
    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.000000"));
        return style;
    }
    
    /**
     * 🎨 ESTILOS CON COLORES Y BOLD PARA KARDEX
     */
    private CellStyle crearEstiloBoldNumero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setBold(true); // Bold para Stock Antes y Stock Actual
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.000"));
        return style;
    }
    
    private CellStyle crearEstiloCantidadIngreso(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setBold(true); // Bold
        font.setColor(IndexedColors.DARK_BLUE.getIndex()); // Azul oscuro para ingresos
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.000"));
        return style;
    }
    
    private CellStyle crearEstiloCantidadSalida(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setBold(true); // Bold
        font.setColor(IndexedColors.RED.getIndex()); // Rojo para salidas
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.000"));
        return style;
    }
    
    private CellStyle crearEstiloValorResumen(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setBold(true); // Bold para resaltar valores
        font.setColor(IndexedColors.DARK_BLUE.getIndex()); // Azul oscuro para valores importantes
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }
    
    private CellStyle crearEstiloValorEntero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setBold(true); // Bold para resaltar valores
        font.setColor(IndexedColors.DARK_BLUE.getIndex()); // Azul oscuro para valores importantes
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0")); // 📊 SIN DECIMALES para enteros
        return style;
    }
    
    /**
     * 🔧 MÉTODOS AUXILIARES
     */
    private String formatearFecha(String fecha) {
        try {
            LocalDate date = LocalDate.parse(fecha);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return fecha; // Devolver original si no se puede parsear
        }
    }
    
    private String construirDetalle(KardexDetalleDTO detalle) {
        StringBuilder descripcion = new StringBuilder();
        
        if (detalle.getType_kardex() == 1) {
            // INGRESO
            descripcion.append("INGRESO - ");
            if (detalle.getDetail_document_ingreso() != null) {
                descripcion.append(detalle.getDetail_document_ingreso());
            }
        } else {
            // SALIDA
            descripcion.append("SALIDA - ");
            if (detalle.getDescripcion() != null) {
                descripcion.append(detalle.getDescripcion());
            }
        }
        
        return descripcion.toString();
    }
} 