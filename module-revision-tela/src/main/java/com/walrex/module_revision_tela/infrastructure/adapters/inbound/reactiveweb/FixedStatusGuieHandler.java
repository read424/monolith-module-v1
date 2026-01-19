package com.walrex.module_revision_tela.infrastructure.adapters.inbound.reactiveweb;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_revision_tela.application.ports.input.FixedStatusGuieUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para el endpoint de corrección de status de guías
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FixedStatusGuieHandler {

    private final FixedStatusGuieUseCase fixedStatusGuieUseCase;

    /**
     * Handler para ejecutar el proceso de corrección de status
     */
    public Mono<ServerResponse> ejecutarCorreccionStatus(ServerRequest request) {
        log.info("Recibida petición POST para ejecutar corrección de status de guías");

        return fixedStatusGuieUseCase.ejecutarCorreccionStatus()
            .flatMap(response -> {
                log.info("Proceso de corrección completado. Total correcciones: {}",
                    response.totalCorreccionesNecesarias());

                return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);
            })
            .onErrorResume(error -> {
                log.error("Error ejecutando corrección de status: {}", error.getMessage(), error);

                return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse(
                        "Error ejecutando corrección de status",
                        error.getMessage()
                    ));
            });
    }

    /**
     * Record para respuestas de error
     */
    private record ErrorResponse(String error, String mensaje) {
    }
}
