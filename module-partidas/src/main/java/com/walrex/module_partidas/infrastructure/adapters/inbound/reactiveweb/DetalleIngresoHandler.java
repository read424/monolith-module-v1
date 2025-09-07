package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_partidas.application.ports.input.ConsultarDetalleIngresoUseCase;
import com.walrex.module_partidas.domain.model.dto.ConsultarDetalleIngresoRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para el endpoint de detalle de ingreso
 * Maneja las peticiones HTTP y valida los datos de entrada
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetalleIngresoHandler {

    private final ConsultarDetalleIngresoUseCase consultarDetalleIngresoUseCase;
    private final Validator validator;

    // Métricas de performance: Contadores de requests
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong validationErrors = new AtomicLong(0);

    /**
     * Consulta el detalle de ingreso con rollos disponibles
     *
     * @param request Petición del servidor
     * @return Respuesta del servidor con los datos
     */
    public Mono<ServerResponse> consultarDetalleIngreso(ServerRequest request) {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();

        try {
            // Extraer parámetros del path
            Integer idPartida = Integer.parseInt(request.queryParam("id_partida").orElse("0"));
            Integer idAlmacen = Integer.parseInt(request.queryParam("id_almacen").orElse("0"));

            // Validar que los parámetros no sean cero
            if (idPartida == 0 || idAlmacen == 0) {
                failedRequests.incrementAndGet();
                log.error("Parámetros requeridos faltantes o inválidos - idPartida: {}, idAlmacen: {}", 
                        idPartida, idAlmacen);
                return ServerResponse.badRequest()
                        .bodyValue("Los parámetros id_partida e id_almacen son requeridos y deben ser mayores a 0");
            }
        
            log.info(
                    "Recibida petición para consultar detalle de ingreso - Partida: {}, Almacén: {} (Total requests: {})",
                    idPartida, idAlmacen, totalRequests.get());

            // Crear request DTO directamente
            ConsultarDetalleIngresoRequest requestDto = ConsultarDetalleIngresoRequest.builder()
                    .idPartida(idPartida)
                    .idAlmacen(idAlmacen)
                    .build();

            // Validar DTO usando Bean Validation
            Set<ConstraintViolation<ConsultarDetalleIngresoRequest>> violations = validator.validate(requestDto);
            if (!violations.isEmpty()) {
                validationErrors.incrementAndGet();
                log.warn("Request inválido (Errores de validación: {}): {}",
                        validationErrors.get(), violations);
                return ServerResponse.badRequest()
                        .bodyValue("Datos de entrada inválidos: " + violations.stream()
                                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                .collect(Collectors.joining(", ")));
            }

            // Ejecutar consulta
            return consultarDetalleIngresoUseCase.consultarDetalleIngreso(requestDto)
                    .flatMap(resultado -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        successfulRequests.incrementAndGet();

                        log.info("Consulta completada con resultado en {}ms (Éxitos: {}, Fallos: {})",
                                responseTime, successfulRequests.get(), failedRequests.get());

                        // Métrica de performance: Tiempo de respuesta del endpoint
                        if (responseTime > 2000) {
                            log.warn("⚠️ ENDPOINT LENTO: /partidas/detalle-ingreso tomó {}ms (más de 2 segundos)",
                                    responseTime);
                        } else if (responseTime > 1000) {
                            log.info("⚠️ ENDPOINT MODERADO: /partidas/detalle-ingreso tomó {}ms (más de 1 segundo)",
                                    responseTime);
                        } else {
                            log.debug("✅ ENDPOINT RÁPIDO: /partidas/detalle-ingreso tomó {}ms", responseTime);
                        }

                        return ServerResponse.ok()
                                .bodyValue(resultado);
                    })
                    .onErrorResume(error -> {
                        long responseTime = System.currentTimeMillis() - startTime;
                        failedRequests.incrementAndGet();

                        log.error(
                                "Error en consulta de detalle de ingreso en {}ms (Total requests: {}, Éxitos: {}, Fallos: {}): {}",
                                responseTime, totalRequests.get(), successfulRequests.get(), failedRequests.get(),
                                error.getMessage());

                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error interno del servidor");
                    });

        } catch (NumberFormatException e) {
            failedRequests.incrementAndGet();
            log.error("Error al parsear parámetros del path: {}", e.getMessage());
            return ServerResponse.badRequest()
                    .bodyValue("Parámetros inválidos: idPartida e idAlmacen deben ser números enteros");
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            failedRequests.incrementAndGet();

            log.error("Error inesperado en consulta de detalle de ingreso en {}ms: {}",
                    responseTime, e.getMessage());

            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue("Error interno del servidor");
        }
    }

    /**
     * Obtiene métricas de performance del handler
     * Útil para monitoreo y debugging
     */
    public void logPerformanceMetrics() {
        long total = totalRequests.get();
        long success = successfulRequests.get();
        long failed = failedRequests.get();
        long validation = validationErrors.get();
        double successRate = total > 0 ? (double) success / total * 100 : 0;

        log.info("📊 MÉTRICAS DE PERFORMANCE - DetalleIngresoHandler:");
        log.info("   Total Requests: {}", total);
        log.info("   Requests Exitosos: {} ({:.2f}%)", success, successRate);
        log.info("   Requests Fallidos: {} ({:.2f}%)", failed, (double) failed / total * 100);
        log.info("   Errores de Validación: {} ({:.2f}%)", validation, (double) validation / total * 100);
    }
}
