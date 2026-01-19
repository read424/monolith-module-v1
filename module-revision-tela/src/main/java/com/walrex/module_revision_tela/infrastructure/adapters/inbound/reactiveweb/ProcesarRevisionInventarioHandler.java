package com.walrex.module_revision_tela.infrastructure.adapters.inbound.reactiveweb;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_revision_tela.application.ports.input.ProcesarRevisionInventarioUseCase;
import com.walrex.module_revision_tela.domain.exceptions.DuplicateRevisionException;
import com.walrex.module_revision_tela.domain.exceptions.NoDataToProcessException;
import com.walrex.module_revision_tela.domain.exceptions.PeriodoNotActiveException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para procesar revisión de inventario
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcesarRevisionInventarioHandler {

    private final ProcesarRevisionInventarioUseCase procesarRevisionInventarioUseCase;

    /**
     * Handler para ejecutar el proceso de revisión de inventario
     */
    public Mono<ServerResponse> procesarRevision(ServerRequest request) {
        log.info("Recibida petición POST para procesar revisión de inventario");

        // Obtener id_usuario del header o usar 26 por defecto
        Integer idUsuario = request.headers().header("X-User-Id").stream()
            .findFirst()
            .map(Integer::parseInt)
            .orElse(26);

        log.debug("Procesando con id_usuario: {}", idUsuario);

        return procesarRevisionInventarioUseCase.procesarRevision(idUsuario)
            .flatMap(response -> {
                log.info("Proceso completado exitosamente. Ordenes procesadas: {}",
                    response.totalOrdenesProcesadas());

                return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);
            })
            .onErrorResume(PeriodoNotActiveException.class, error -> {
                log.warn("No hay periodo activo: {}", error.getMessage());
                return ServerResponse.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse("Periodo no activo", error.getMessage()));
            })
            .onErrorResume(NoDataToProcessException.class, error -> {
                log.warn("No hay datos para procesar: {}", error.getMessage());
                return ServerResponse.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse("Sin datos", error.getMessage()));
            })
            .onErrorResume(DuplicateRevisionException.class, error -> {
                log.warn("Duplicidad detectada: {}", error.getMessage());
                return ServerResponse.status(409)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse("Duplicado", error.getMessage()));
            })
            .onErrorResume(error -> {
                log.error("Error procesando revisión de inventario: {}", error.getMessage(), error);
                return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ErrorResponse(
                        "Error interno",
                        "Error procesando revisión de inventario: " + error.getMessage()
                    ));
            });
    }

    /**
     * Record para respuestas de error
     */
    private record ErrorResponse(String error, String mensaje) {
    }
}
