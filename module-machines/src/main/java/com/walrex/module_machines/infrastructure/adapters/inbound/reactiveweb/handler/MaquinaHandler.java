package com.walrex.module_machines.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.module_machines.application.ports.input.ListMaquinasUseCase;
import com.walrex.module_machines.domain.exceptions.MaquinaException;
import com.walrex.module_machines.infrastructure.adapters.inbound.reactiveweb.dto.response.MaquinaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaquinaHandler {

    private final ListMaquinasUseCase listMaquinasUseCase;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        String search = request.queryParam("search").orElse("").trim();
        int page = Integer.parseInt(request.queryParam("page").orElse("1"));
        int size = Integer.parseInt(request.queryParam("size").orElse("20"));

        String idUbicacionParam = request.queryParam("idUbicacion").orElse(null);
        if (idUbicacionParam == null || idUbicacionParam.isBlank()) {
            return ServerResponse.badRequest().bodyValue("El parámetro idUbicacion es obligatorio");
        }

        Integer idUbicacion;
        try {
            idUbicacion = Integer.valueOf(idUbicacionParam);
        } catch (NumberFormatException e) {
            return ServerResponse.badRequest().bodyValue("El parámetro idUbicacion debe ser un número entero");
        }

        return listMaquinasUseCase.listByUbicacion(idUbicacion, search, page, size)
                .map(paged -> paged.map(m -> MaquinaResponse.builder()
                        .idMaquina(m.getIdMaquina())
                        .descMaq(m.getDescMaq())
                        .build()))
                .flatMap(paged -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(paged))
                .onErrorResume(this::buildErrorResponse);
    }

    private Mono<ServerResponse> buildErrorResponse(Throwable error) {
        if (error instanceof MaquinaException ex) {
            return switch (ex.getCode()) {
                case "NOT_FOUND" -> ServerResponse.status(404).bodyValue(ex.getMessage());
                default -> ServerResponse.badRequest().bodyValue(ex.getMessage());
            };
        }
        log.error("Error inesperado en MaquinaHandler: {}", error.getMessage(), error);
        return ServerResponse.status(500).bodyValue("Error interno al procesar la solicitud");
    }
}
