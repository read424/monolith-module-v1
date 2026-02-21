package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.GuideAdditionUseCase;
import com.walrex.module_almacen.domain.model.dto.AddGuideRequest;
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
public class GuideAdditionHandler {

    private final GuideAdditionUseCase guideAdditionUseCase;

    public Mono<ServerResponse> addGuide(ServerRequest request) {
        log.info("Recibida solicitud para agregar guía");
        
        return request.bodyToMono(AddGuideRequest.class)
                .flatMap(guideAdditionUseCase::addGuide)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(e -> {
                    log.error("Error al agregar guía: {}", e.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("Error al registrar la guía: " + e.getMessage());
                });
    }
}
