package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.ObtenerSessionArticuloPesajeUseCase;
import com.walrex.module_almacen.application.ports.input.PesajeUseCase;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import com.walrex.module_almacen.domain.model.exceptions.ArticuloCompletadoException;
import com.walrex.module_almacen.domain.model.exceptions.SessionPesajeInvalidaException;
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
public class PesajeHandler {

    private final PesajeUseCase pesajeUseCase;
    private final ObtenerSessionArticuloPesajeUseCase obtenerSessionUseCase;

    public Mono<ServerResponse> registrarPesaje(ServerRequest request) {
        return request.bodyToMono(PesajeRequest.class)
                .flatMap(pesajeUseCase::registrarPesaje)
                .flatMap(detalle -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(detalle))
                .onErrorResume(e -> {
                    log.error("Error en PesajeHandler: {}", e.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(e.getMessage());
                });
    }

    public Mono<ServerResponse> getSessionArticuloPesaje(ServerRequest request) {
        return request.queryParam("id_detordeningreso")
                .map(param -> {
                    try {
                        Integer idDetOrdenIngreso = Integer.parseInt(param);
                        return obtenerSessionUseCase.obtenerSession(idDetOrdenIngreso)
                                .flatMap(response -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(response))
                                .onErrorResume(ArticuloCompletadoException.class, e -> {
                                    log.warn("Artículo completado: {}", e.getMessage());
                                    return ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(Map.of("message", e.getMessage()));
                                })
                                .onErrorResume(SessionPesajeInvalidaException.class, e -> {
                                    log.error("Sesión de pesaje inválida: {}", e.getMessage());
                                    return ServerResponse.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(Map.of("error", e.getMessage()));
                                })
                                .onErrorResume(e -> {
                                    log.error("Error al obtener sesión de pesaje: {}", e.getMessage());
                                    return ServerResponse.badRequest()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(Map.of("error", e.getMessage()));
                                });
                    } catch (NumberFormatException e) {
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", "El parámetro id_detordeningreso debe ser un número entero"));
                    }
                })
                .orElseGet(() -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", "El parámetro id_detordeningreso es requerido")));
    }
}
