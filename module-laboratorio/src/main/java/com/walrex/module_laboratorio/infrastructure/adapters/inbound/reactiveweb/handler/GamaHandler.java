package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.module_laboratorio.application.ports.input.*;
import com.walrex.module_laboratorio.domain.exceptions.GamaException;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.GamaCreateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.GamaUpdateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper.GamaRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class GamaHandler {

    private final CreateGamaUseCase createUseCase;
    private final GetGamaByIdUseCase getByIdUseCase;
    private final ListGamasUseCase listAllUseCase;
    private final ListActiveGamasUseCase listActiveUseCase;
    private final UpdateGamaUseCase updateUseCase;
    private final DeleteGamaUseCase deleteUseCase;

    private final GamaRestMapper mapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(GamaCreateRequest.class)
                .map(mapper::toDomain)
                .flatMap(createUseCase::create)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.created(URI.create("/laboratorio/gamas/" + response.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return getByIdUseCase.getById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        return listAllUseCase.listAll(page, size)
                .map(paged -> paged.map(mapper::toResponse))
                .flatMap(paged -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(paged))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> findActive(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        return listActiveUseCase.listActive(page, size)
                .map(paged -> paged.map(mapper::toResponse))
                .flatMap(paged -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(paged))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return request.bodyToMono(GamaUpdateRequest.class)
                .map(mapper::toDomain)
                .flatMap(gama -> updateUseCase.update(id, gama))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return deleteUseCase.delete(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(this::buildErrorResponse);
    }

    private Mono<ServerResponse> buildErrorResponse(Throwable error) {
        if (error instanceof GamaException gamaException) {
            return switch (gamaException.getCode()) {
                case "NOT_FOUND" -> ServerResponse.status(404).bodyValue(gamaException.getMessage());
                case "DUPLICATE_NAME" -> ServerResponse.status(409).bodyValue(gamaException.getMessage());
                default -> ServerResponse.badRequest().bodyValue(gamaException.getMessage());
            };
        }
        log.error("Error inesperado en GamaHandler: {} - {}", error.getClass().getSimpleName(), error.getMessage(), error);
        return ServerResponse.status(500).bodyValue("Error interno al procesar la solicitud");
    }
}
