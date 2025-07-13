package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_almacen.application.ports.input.GenerarGuiaRemisionUseCase;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.GuiaRemisionRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.GenerarGuiaRemisionRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuiaRemisionHandler {

    private final GuiaRemisionRequestMapper guiaRemisionRequestMapper;
    private final GenerarGuiaRemisionUseCase generarGuiaRemisionUseCase;

    public Mono<ServerResponse> generarGuiaRemision(ServerRequest request) {
        log.info("📥 POST /almacen/ordenes-salida-devolucion/generar-guia");

        return request.bodyToMono(GenerarGuiaRemisionRequest.class)
                .doOnNext(req -> log.debug("📥 Request recibido: {}", req))
                .map(guiaRemisionRequestMapper::toDomainDTO)
                .flatMap(generarGuiaRemisionUseCase::generarGuiaRemision)
                .flatMap(resultado -> {
                    log.info("✅ Guía de remisión generada exitosamente para orden: {}", resultado.getIdOrdenSalida());
                    return ServerResponse.ok().bodyValue(resultado);
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("⚠️ Error de validación: {}", ex.getMessage());
                    return ServerResponse.badRequest().bodyValue(Map.of(
                            "error", "Datos inválidos",
                            "message", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.error("❌ Error al generar guía de remisión: ", ex);
                    return ServerResponse.status(500).bodyValue(Map.of(
                            "error", "Error interno del servidor",
                            "message", "No se pudo generar la guía de remisión"));
                });
    }

}
