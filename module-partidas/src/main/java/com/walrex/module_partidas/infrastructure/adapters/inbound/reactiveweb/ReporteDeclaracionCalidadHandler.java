package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_partidas.application.ports.input.ReporteDeclaracionCalidadUseCase;
import com.walrex.module_partidas.infrastructure.adapters.outbound.excel.DeclaracionCalidadExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteDeclaracionCalidadHandler {

    private final ReporteDeclaracionCalidadUseCase reporteUseCase;
    private final DeclaracionCalidadExcelService excelService;

    public Mono<ServerResponse> exportarExcel(ServerRequest request) {
        String fechaDeclaracion = request.queryParam("fecha_declaracion").orElse(null);
        if (fechaDeclaracion == null || fechaDeclaracion.isBlank()) {
            return ServerResponse.badRequest()
                    .bodyValue("El parámetro fecha_declaracion es obligatorio (formato: yyyy-MM-dd)");
        }

        Integer idUbicacion = request.queryParam("id_ubicacion")
                .map(v -> {
                    try { return Integer.valueOf(v); }
                    catch (NumberFormatException e) { return null; }
                })
                .orElse(null);

        try {
            java.time.LocalDate.parse(fechaDeclaracion);
        } catch (DateTimeParseException e) {
            return ServerResponse.badRequest()
                    .bodyValue("fecha_declaracion debe tener formato yyyy-MM-dd");
        }

        log.info("Exportando reporte declaracion calidad: fecha={}, idUbicacion={}", fechaDeclaracion, idUbicacion);

        String filename = String.format("reporte_declaraciones_calidad_%s.xlsx",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return reporteUseCase.obtenerReporte(idUbicacion, fechaDeclaracion)
                .collectList()
                .flatMap(registros -> {
                    if (registros.isEmpty()) {
                        return ServerResponse.status(HttpStatus.NO_CONTENT).build();
                    }
                    return excelService.generarExcel(registros, fechaDeclaracion,
                                    request.exchange().getResponse().bufferFactory())
                            .collectList()
                            .flatMap(buffers -> ServerResponse.ok()
                                    .header(HttpHeaders.CONTENT_TYPE,
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                                    .header(HttpHeaders.CONTENT_DISPOSITION,
                                            "attachment; filename=\"" + filename + "\"")
                                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                                    .body(Flux.fromIterable(buffers),
                                            org.springframework.core.io.buffer.DataBuffer.class));
                })
                .onErrorResume(error -> {
                    log.error("Error exportando reporte declaracion calidad: {}", error.getMessage(), error);
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue("Error generando reporte Excel");
                });
    }
}
