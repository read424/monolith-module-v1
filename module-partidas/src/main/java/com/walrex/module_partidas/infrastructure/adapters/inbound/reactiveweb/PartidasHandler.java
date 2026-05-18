package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_partidas.application.ports.input.CrearDeclaracionCalidadUseCase;
import com.walrex.module_partidas.application.ports.input.ListPartidasByUbicacionUseCase;
import com.walrex.module_partidas.application.ports.input.ListPartidasSimpleUseCase;
import com.walrex.module_partidas.application.ports.input.ListRollosByPartidaUseCase;
import com.walrex.module_partidas.domain.model.DeclaracionCalidad;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.CrearDeclaracionCalidadRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_partidas.application.ports.input.ListPartidasUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidasHandler {

    private final ListPartidasUseCase listPartidasUseCase;
    private final ListPartidasByUbicacionUseCase listPartidasByUbicacionUseCase;
    private final ListPartidasSimpleUseCase listPartidasSimpleUseCase;
    private final ListRollosByPartidaUseCase listRollosByPartidaUseCase;
    private final CrearDeclaracionCalidadUseCase crearDeclaracionCalidadUseCase;

    public Mono<ServerResponse> listPartidas(ServerRequest request) {
        try {
            int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
            int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
            String search = request.queryParam("search").orElse("");

            return listPartidasUseCase.listPartidas(search, page, size)
                    .flatMap(response -> ServerResponse.ok().bodyValue(response))
                    .onErrorResume(error -> {
                        log.error("Error listando partidas typeahead: {}", error.getMessage(), error);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error interno del servidor");
                    });
        } catch (NumberFormatException ex) {
            return ServerResponse.badRequest().bodyValue("Los parámetros page y size deben ser numéricos");
        }
    }

    public Mono<ServerResponse> declararCalidad(ServerRequest request) {
        try {
            String idUbicacionParam = request.queryParam("idubicacion").orElse(null);
            String fechaParam = request.queryParam("fechaprograma").orElse(null);

            if (idUbicacionParam == null || idUbicacionParam.isBlank()) {
                return ServerResponse.badRequest().bodyValue("El parámetro idUbicacion es obligatorio");
            }
            if (fechaParam == null || fechaParam.isBlank()) {
                return ServerResponse.badRequest().bodyValue("El parámetro fechaprograma es obligatorio (formato: yyyy-MM-dd)");
            }

            Integer idUbicacion = Integer.valueOf(idUbicacionParam);
            LocalDate fecha = LocalDate.parse(fechaParam);
            String search = request.queryParam("search").orElse(null);
            int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
            int size = request.queryParam("size").map(Integer::parseInt).orElse(10);

            return listPartidasByUbicacionUseCase.listByUbicacion(idUbicacion, fecha, search, page, size)
                    .flatMap(response -> ServerResponse.ok().bodyValue(response))
                    .onErrorResume(error -> {
                        log.error("Error listando partidas por ubicacion: {}", error.getMessage(), error);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error interno del servidor");
                    });
        } catch (NumberFormatException ex) {
            return ServerResponse.badRequest().bodyValue("idUbicacion debe ser un número entero");
        } catch (DateTimeParseException ex) {
            return ServerResponse.badRequest().bodyValue("fechaprograma debe tener formato yyyy-MM-dd");
        }
    }

    public Mono<ServerResponse> listPartidasSimple(ServerRequest request) {
        try {
            String search = request.queryParam("search").orElse(null);
            int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
            int size = request.queryParam("size").map(Integer::parseInt).orElse(10);

            return listPartidasSimpleUseCase.listPartidas(search, page, size)
                    .flatMap(response -> ServerResponse.ok().bodyValue(response))
                    .onErrorResume(error -> {
                        log.error("Error listando partidas: {}", error.getMessage(), error);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error interno del servidor");
                    });
        } catch (NumberFormatException ex) {
            return ServerResponse.badRequest().bodyValue("Los parámetros page y size deben ser numéricos");
        }
    }

    public Mono<ServerResponse> listRollosByPartida(ServerRequest request) {
        try {
            Integer idPartida = Integer.valueOf(request.pathVariable("idPartida"));

            return listRollosByPartidaUseCase.listRollos(idPartida)
                    .collectList()
                    .flatMap(rollos -> ServerResponse.ok().bodyValue(rollos))
                    .onErrorResume(error -> {
                        log.error("Error listando rollos de partida {}: {}", request.pathVariable("idPartida"), error.getMessage(), error);
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error interno del servidor");
                    });
        } catch (NumberFormatException ex) {
            return ServerResponse.badRequest().bodyValue("idPartida debe ser un número entero");
        }
    }

    public Mono<ServerResponse> crearDeclaracionCalidad(ServerRequest request) {
        return request.bodyToMono(CrearDeclaracionCalidadRequest.class)
                .flatMap(body -> {
                    if (body.getIdUbicacion() == null) {
                        return ServerResponse.badRequest().bodyValue("El campo id_ubicacion es obligatorio");
                    }
                    if (body.getFechaDeclaracion() == null) {
                        return ServerResponse.badRequest().bodyValue("El campo fecha_declaracion es obligatorio");
                    }
                    if (body.getIdPartida() == null) {
                        return ServerResponse.badRequest().bodyValue("El campo id_partida es obligatorio");
                    }

                    DeclaracionCalidad declaracion = DeclaracionCalidad.builder()
                            .idUbicacion(body.getIdUbicacion())
                            .fechaDeclaracion(body.getFechaDeclaracion())
                            .idPartida(body.getIdPartida())
                            .idMaquina(body.getIdMaquina())
                            .idAuditor(body.getIdAuditor())
                            .nivelCritico(body.getNivelCritico())
                            .idMotivoRechazo(body.getIdMotivoRechazo())
                            .isObservado(body.getIsObservado() != null ? body.getIsObservado() : 0)
                            .observacion(body.getObservacion())
                            .cntRollos(body.getCntRollos())
                            .status(body.getStatus() != null ? body.getStatus() : 1)
                            .build();

                    return crearDeclaracionCalidadUseCase.crear(declaracion)
                            .flatMap(saved -> ServerResponse.status(HttpStatus.CREATED)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(saved))
                            .onErrorResume(DataIntegrityViolationException.class, ex -> {
                                log.warn("Declaración duplicada para partida {} / ubicacion {} / fecha {}",
                                        body.getIdPartida(), body.getIdUbicacion(), body.getFechaDeclaracion());
                                return ServerResponse.status(HttpStatus.CONFLICT)
                                        .bodyValue("Ya existe una declaración para esta partida en la fecha y ubicación indicadas");
                            })
                            .onErrorResume(error -> {
                                log.error("Error al crear declaracion de calidad: {}", error.getMessage(), error);
                                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .bodyValue("Error interno del servidor");
                            });
                });
    }
}
