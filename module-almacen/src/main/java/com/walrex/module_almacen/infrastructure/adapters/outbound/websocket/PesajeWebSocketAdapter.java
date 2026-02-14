package com.walrex.module_almacen.infrastructure.adapters.outbound.websocket;

import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.infrastructure.adapters.outbound.websocket.dto.WebSocketPesajeNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PesajeWebSocketAdapter implements PesajeNotificationPort {

    private final WebClient webClient;

    @Value("${websocket.api.url:http://127.0.0.1:3355/api/ws}")
    private String websocketApiUrl;

    @Override
    public Mono<Void> notifyWeightRegistered(PesajeDetalle pesaje) {
        WebSocketPesajeNotification notification = WebSocketPesajeNotification.builder()
                .tipo("PESO_REGISTRADO")
                .data(pesaje)
                .build();

        log.info("Enviando notificación WebSocket de pesaje para rollo: {}", pesaje.getCod_rollo());

        return webClient.post()
                .uri(websocketApiUrl + "/send-message")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Notificación WebSocket enviada exitosamente"))
                .doOnError(e -> log.error("Error al enviar notificación WebSocket: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty()); // No bloquear el flujo si falla el WebSocket
    }
}
