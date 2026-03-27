package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.GuideAdditionUseCase;
import com.walrex.module_almacen.application.ports.input.UpdateGuideArticleUseCase;
import com.walrex.module_almacen.domain.model.dto.AddGuideRequest;
import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleConflictException;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuideAdditionHandler {

    private final GuideAdditionUseCase guideAdditionUseCase;
    private final UpdateGuideArticleUseCase updateGuideArticleUseCase;

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

    public Mono<ServerResponse> updateGuideArticle(ServerRequest request) {
        try {
            Integer idDetalleOrden = Integer.parseInt(request.pathVariable("idDetalleOrden"));
            return request.bodyToMono(UpdateGuideArticleRequest.class)
                    .flatMap(body -> updateGuideArticleUseCase.updateGuideArticle(idDetalleOrden, body))
                    .then(ServerResponse.ok().build())
                    .onErrorResume(GuideArticleNotFoundException.class, e -> ServerResponse.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", e.getMessage())))
                    .onErrorResume(GuideArticleConflictException.class, e -> ServerResponse.status(HttpStatus.CONFLICT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", e.getMessage())))
                    .onErrorResume(e -> {
                        log.error("Error al actualizar artículo de guía: {}", e.getMessage(), e);
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", e.getMessage()));
                    });
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("error", "El parámetro idDetalleOrden debe ser un número entero"));
        }
    }
}
