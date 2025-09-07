package com.walrex.module_partidas.infrastructure.adapters.outbound.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.walrex.module_partidas.application.ports.output.WebSocketNotificationPort;
import com.walrex.module_partidas.domain.exceptions.WebSocketNotificationException;
import com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto.WebSocketNotificationRequest;
import com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto.WebSocketNotificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements WebSocketNotificationPort {
    private final WebClient webClient;

    @Value("${websocket.api.url}")
    private String websocketApiUrl;

    @Override
    public Mono<Void> enviarNotificacionAlmacen(WebSocketNotificationRequest request) {
        log.info("Enviando notificación WebSocket: {}", request);

        return webClient
            .post()
            .uri(websocketApiUrl + "/send-message")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(WebSocketNotificationResponse.class)
            .doOnSuccess(response -> log.info("Notificación WebSocket enviada exitosamente: {}", response))
            .doOnError(error -> log.error("Error enviando notificación WebSocket: {}", error.getMessage()))
            .then()
            .onErrorMap(throwable -> new WebSocketNotificationException(
                "No se pudo enviar la notificación al siguiente almacén: " + throwable.getMessage(),
                throwable));
    }
}
