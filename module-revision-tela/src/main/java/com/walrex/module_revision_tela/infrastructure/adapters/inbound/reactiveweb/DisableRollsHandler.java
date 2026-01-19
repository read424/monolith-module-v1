package com.walrex.module_revision_tela.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_revision_tela.application.ports.input.DisableUnliftedRollsUseCase;
import com.walrex.module_revision_tela.domain.model.dto.DisableRollsRequest;
import com.walrex.module_revision_tela.domain.model.dto.DisableRollsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisableRollsHandler {

    private final DisableUnliftedRollsUseCase disableUnliftedRollsUseCase;

    /**
     * Handler para POST /api/v1/revision_tela/disabled_rolls
     * Deshabilita los rollos sin levantamiento asignado para un periodo
     */
    public Mono<ServerResponse> disableUnliftedRolls(ServerRequest request) {
        log.info("[Handler] Recibida solicitud para deshabilitar rollos sin levantamiento");

        return request.bodyToMono(DisableRollsRequest.class)
            .doOnNext(req -> log.debug("[Handler] Request recibido: idPeriodo={}", req.getIdPeriodo()))
            .flatMap(req -> {
                if (req.getIdPeriodo() == null) {
                    log.warn("[Handler] idPeriodo es requerido");
                    return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                            "error", "Parámetro requerido",
                            "mensaje", "El campo idPeriodo es obligatorio"
                        ));
                }

                return disableUnliftedRollsUseCase.disableUnliftedRolls(req.getIdPeriodo())
                    .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                    )
                    .onErrorResume(error -> {
                        log.error("[Handler] Error procesando solicitud: {}", error.getMessage(), error);
                        return ServerResponse.status(500)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "error", "Error interno",
                                "mensaje", "Error deshabilitando rollos: " + error.getMessage()
                            ));
                    });
            })
            .onErrorResume(error -> {
                log.error("[Handler] Error parseando request: {}", error.getMessage(), error);
                return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "error", "Request inválido",
                        "mensaje", "Error parseando el body del request: " + error.getMessage()
                    ));
            });
    }
}
