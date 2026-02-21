package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.RegisterGuideNoRollsUseCase;
import com.walrex.module_almacen.domain.model.dto.RegisterGuideNoRollsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGuideNoRollsHandler {

    private final RegisterGuideNoRollsUseCase useCase;

    public Mono<ServerResponse> registerGuide(ServerRequest request) {
        return request.bodyToMono(RegisterGuideNoRollsRequest.class)
                .flatMap(useCase::registerGuide)
                .then(ServerResponse.status(HttpStatus.CREATED).build())
                .onErrorResume(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("Idempotency conflict")) {
                        return ServerResponse.status(HttpStatus.CONFLICT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"error\": \"Conflict\", \"message\": \"" + e.getMessage() + "\"}");
                    }
                    log.error("Error al registrar guía básica: {}", e.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("{\"error\": \"Bad Request\", \"message\": \"" + e.getMessage() + "\"}");
                });
    }
}
