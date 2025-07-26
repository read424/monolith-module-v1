package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.ConsultarKardexUseCase;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.ConsultarKardexRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ConsultarKardexRequest;
import com.walrex.module_almacen.infrastructure.adapters.outbound.excel.KardexExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class KardexExcelHandler {
    private final Validator validator;
    private final ConsultarKardexUseCase consultarKardexUseCase;
    private final ConsultarKardexRequestMapper kardexRequestMapper;
    private final KardexExcelService kardexExcelService;

    public Mono<ServerResponse> exportarKardexExcel(ServerRequest request) {
        log.info("📊 Exportando kardex a Excel - Método HTTP: {}", request.method());
        log.info("🔍 Query params: {}", request.queryParams());
        log.info("🐛 DEBUG: Parámetro ids_articulos_filtrados: {}", 
            request.queryParam("ids_articulos_filtrados").orElse("NO ENCONTRADO"));

        return Mono.just(kardexRequestMapper.extractFromQuery(request))
                .doOnNext(dto -> log.info("Request params recibidos para Excel: {}", dto))
                .flatMap(this::validate)
                .map(kardexRequestMapper::toCriterios)
                .flatMap(consultarKardexUseCase::consultarKardex)
                .flatMap(kardexReporte -> {
                    // 🔍 VERIFICAR SI HAY FILTRO DE PRODUCTOS
                    var articulosParaExportar = kardexReporte.getArticulos();
                    var idsArticulosFiltrados = request.queryParam("ids_articulos_filtrados");
                    
                    if (idsArticulosFiltrados.isPresent()) {
                        log.info("🎯 Filtro de productos detectado: {}", idsArticulosFiltrados.get());
                        
                        // Parsear los IDs (vienen como string separado por comas)
                        String[] idsArray = idsArticulosFiltrados.get().split(",");
                        log.info("🐛 DEBUG: IDs parseados: {}", java.util.Arrays.toString(idsArray));
                        
                        // DEBUG: Ver estructura de algunos artículos
                        if (!kardexReporte.getArticulos().isEmpty()) {
                            var primerArticulo = kardexReporte.getArticulos().get(0);
                            log.info("🐛 DEBUG: Primer artículo - ID: {}, Código: {}", 
                                primerArticulo.getIdArticulo(), primerArticulo.getCodArticulo());
                        }
                        
                        // Filtrar artículos por los IDs especificados
                        articulosParaExportar = kardexReporte.getArticulos().stream()
                            .filter(articulo -> {
                                String articuloId = String.valueOf(articulo.getIdArticulo());
                                log.debug("🐛 DEBUG: Comparando artículo ID {} con filtros", articuloId);
                                for (String id : idsArray) {
                                    if (articuloId.equals(id.trim())) {
                                        log.debug("🐛 DEBUG: ✅ Artículo {} incluido en filtro", articuloId);
                                        return true;
                                    }
                                }
                                log.debug("🐛 DEBUG: ❌ Artículo {} excluido del filtro", articuloId);
                                return false;
                            })
                            .collect(java.util.stream.Collectors.toList());
                        
                        log.info("📊 Productos filtrados: {} de {} total", 
                            articulosParaExportar.size(), kardexReporte.getArticulos().size());
                        
                        if (articulosParaExportar.isEmpty()) {
                            log.warn("⚠️ WARNING: No se encontraron productos que coincidan con los filtros");
                        }
                    } else {
                        log.info("📋 Exportando todos los productos: {}", articulosParaExportar.size());
                    }
                    
                    // Generar nombre de archivo único
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String filename = String.format("reporte_kardex_%s.xlsx", timestamp);
                    
                    log.info("📁 Generando archivo Excel: {}", filename);
                    
                    // Generar Excel con artículos filtrados (o todos si no hay filtro)
                    return kardexExcelService.generarExcelKardex(
                            articulosParaExportar,
                            request.queryParam("fecha_inicio").orElse(""),
                            request.queryParam("fecha_fin").orElse(""),
                            request.exchange().getResponse().bufferFactory()
                    )
                    .flatMap(excelDataBuffer -> {
                        log.info("✅ Excel generado exitosamente: {}", filename);
                        
                        return ServerResponse.status(HttpStatus.OK)
                                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                                .body(excelDataBuffer, org.springframework.core.io.buffer.DataBuffer.class);
                    });
                })
                .doOnSuccess(response -> log.info("📄 Respuesta Excel enviada exitosamente"))
                .doOnError(error -> log.error("❌ Error exportando kardex a Excel: {}", error.getMessage(), error));
    }

    private Mono<ConsultarKardexRequest> validate(ConsultarKardexRequest dto) {
        var errors = new BeanPropertyBindingResult(dto, ConsultarKardexRequest.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            var errorMessages = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .toList();
            return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
        }
        return Mono.just(dto);
    }
} 