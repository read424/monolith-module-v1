package com.walrex.despacho.module_liquidaciones.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.despacho.module_liquidaciones.application.ports.input.GenerarReporteDespachoSalidaUseCase;
import com.walrex.despacho.module_liquidaciones.infrastructure.adapters.inbound.reactiveweb.request.ReporteDespachoSalidaRequest;
import com.walrex.despacho.module_liquidaciones.infrastructure.adapters.outbound.report.DespachoSalidaExcelReportGenerator;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import com.walrex.module_security_commons.infrastructure.adapters.JwtUserContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteDespachoHandler {

    private final GenerarReporteDespachoSalidaUseCase generarReporteDespachoSalidaUseCase;
    private final DespachoSalidaExcelReportGenerator excelReportGenerator;
    private final JwtUserContextService jwtUserContextService;

    private static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DataBufferFactory DATA_BUFFER_FACTORY = new DefaultDataBufferFactory();

    public Mono<ServerResponse> generarReporteDespachoSalidas(ServerRequest request) {
        JwtUserInfo user = jwtUserContextService.getCurrentUser(request);
        log.info("Iniciando generación de reporte de despacho salidas - Usuario: {} (ID: {})",
            user.getUsername(), user.getUserId());

        return request.bodyToMono(ReporteDespachoSalidaRequest.class)
            .doOnNext(this::validateRequest)
            .flatMap(req -> {
                LocalDate startDate = req.getStartDate();
                LocalDate endDate = req.getEndDate();
                Integer conGuia = req.getConGuia();
                Integer idCliente = req.getIdCliente();

                String fileName = generateFileName(startDate, endDate);

                LocalDate finalStartDate = startDate;
                LocalDate finalEndDate = endDate;

                return generarReporteDespachoSalidaUseCase.generarReporte(startDate, endDate, conGuia, idCliente)
                    .collectList()
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap(data -> {
                        // Si no hay registros, devolver respuesta JSON
                        if (data.isEmpty()) {
                            log.info("No se encontraron registros para el reporte");
                            return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(new EmptyReportResponse(
                                    false,
                                    "No se encontraron registros para los criterios de búsqueda especificados",
                                    0
                                ));
                        }

                        // Si hay registros, generar Excel
                        log.info("Generando Excel con {} registros", data.size());
                        byte[] excelBytes = excelReportGenerator.generateReport(data, finalStartDate, finalEndDate);
                        DataBuffer dataBuffer = DATA_BUFFER_FACTORY.wrap(excelBytes);

                        return ServerResponse.ok()
                            .contentType(MediaType.parseMediaType(CONTENT_TYPE_EXCEL))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(excelBytes.length))
                            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                            .header(HttpHeaders.PRAGMA, "no-cache")
                            .header(HttpHeaders.EXPIRES, "0")
                            .bodyValue(dataBuffer);
                    });
            })
            .onErrorResume(IllegalArgumentException.class, e -> {
                log.warn("Parámetros inválidos: {}", e.getMessage());
                return ServerResponse.badRequest()
                    .bodyValue(new ErrorResponse("Parámetros inválidos", e.getMessage()));
            })
            .onErrorResume(e -> {
                log.error("Error generando reporte: {}", e.getMessage(), e);
                return ServerResponse.status(500)
                    .bodyValue(new ErrorResponse("Error interno", "Error generando el reporte de despacho"));
            });
    }

    private void validateRequest(ReporteDespachoSalidaRequest request) {
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("El campo startDate es requerido");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("El campo endDate es requerido");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("startDate no puede ser mayor que endDate");
        }
        if (request.getConGuia() != null && request.getConGuia() != 0 && request.getConGuia() != 1) {
            throw new IllegalArgumentException("El campo con_guia debe ser 0 o 1");
        }
    }

    private String generateFileName(LocalDate startDate, LocalDate endDate) {
        String initStr = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endStr = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("reporte_despacho_salidas_%s_%s.xlsx", initStr, endStr);
    }

    private record ErrorResponse(String error, String message) {}

    private record EmptyReportResponse(boolean hasData, String message, int totalRecords) {}
}
