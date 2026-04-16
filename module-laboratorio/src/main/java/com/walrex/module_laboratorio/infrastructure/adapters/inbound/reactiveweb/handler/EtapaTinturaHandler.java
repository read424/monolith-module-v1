package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.module_laboratorio.application.ports.input.EtapaTinturaUseCase;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.EtapaTinturaRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper.EtapaTinturaRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class EtapaTinturaHandler {

    private final EtapaTinturaUseCase useCase;
    private final EtapaTinturaRestMapper mapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(EtapaTinturaRequest.class)
                .map(mapper::toDomain)
                .flatMap(useCase::create)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.created(URI.create("/laboratorio/etapa-tintura/" + response.getId_tintura()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(e -> {
                    log.error("Error al crear etapa de tintura", e);
                    return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(e.getMessage());
                });
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return useCase.findById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        return useCase.findAll(page, size)
                .map(mapper::toResponse)
                .collectList()
                .flatMap(list -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(list));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return request.bodyToMono(EtapaTinturaRequest.class)
                .map(mapper::toDomain)
                .flatMap(etapa -> useCase.update(id, etapa))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(e -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return useCase.delete(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(e -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }
}
