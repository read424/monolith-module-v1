package com.walrex.module_revision_tela.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_revision_tela.application.ports.input.ReviewInventoryAndLiftingUseCase;
import com.walrex.module_revision_tela.domain.exceptions.InsufficientRollosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para ejecutar el análisis de inventario y levantamiento
 *
 * Cruza la información de los rollos inventariados con los registros de levantamiento
 * y asigna el id_levantamiento correspondiente a cada rollo
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisInventoryLiftingHandler {

    private final ReviewInventoryAndLiftingUseCase reviewInventoryAndLiftingUseCase;

    /**
     * Handler para ejecutar el análisis de inventario levantado
     *
     * @param request ServerRequest con el idPeriodo en el body
     * @return Mono con ServerResponse indicando el resultado del proceso
     */
    public Mono<ServerResponse> executeAnalysis(ServerRequest request) {
        log.info("Recibida petición POST para ejecutar análisis de inventario levantado");

        return request.bodyToMono(AnalysisRequest.class)
            .doOnNext(req -> log.debug("Ejecutando análisis para periodo: {}", req.idPeriodo()))
            .flatMap(req -> reviewInventoryAndLiftingUseCase.executeAnalysis(req.idPeriodo())
                .flatMap(response -> {
                    log.info("Análisis completado - Periodo: {}, Levantamientos: {}, Rollos: {}",
                        response.idPeriodo(), response.totalLevantamientosProcesados(), response.totalRollosAsignados());

                    return ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response);
                })
            )
            .onErrorResume(InsufficientRollosException.class, error -> {
                log.error("Rollos insuficientes - Levantamiento: {}, Requeridos: {}, Disponibles: {}",
                    error.getIdLevantamiento(), error.getRollosRequeridos(), error.getRollosDisponibles());

                return ServerResponse.status(409)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new InsufficientRollosErrorResponse(
                        "Rollos insuficientes",
                        error.getMessage(),
                        error.getIdLevantamiento(),
                        error.getRollosRequeridos(),
                        error.getRollosDisponibles()
                    ));
            })
            .onErrorResume(IllegalArgumentException.class, error -> {
                log.warn("Parámetro inválido: {}", error.getMessage());
                return ServerResponse.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse("Parámetro inválido", error.getMessage()));
            })
            .onErrorResume(error -> {
                log.error("Error ejecutando análisis de inventario levantado: {}", error.getMessage(), error);
                return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse(
                        "Error interno",
                        "Error ejecutando análisis: " + error.getMessage()
                    ));
            });
    }

    /**
     * Record para la solicitud con el ID del periodo
     */
    private record AnalysisRequest(Integer idPeriodo) {
    }

    /**
     * Record para respuestas de error genéricas
     */
    private record ErrorResponse(String error, String mensaje) {
    }

    /**
     * Record para error específico de rollos insuficientes
     */
    private record InsufficientRollosErrorResponse(
        String error,
        String mensaje,
        Integer idLevantamiento,
        Integer rollosRequeridos,
        Integer rollosDisponibles
    ) {
    }
}
