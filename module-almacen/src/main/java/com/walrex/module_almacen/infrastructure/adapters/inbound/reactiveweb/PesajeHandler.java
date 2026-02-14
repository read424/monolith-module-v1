package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.PesajeUseCase;
import com.walrex.module_almacen.domain.model.dto.PesajeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PesajeHandler {

    private final PesajeUseCase pesajeUseCase;

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
}
