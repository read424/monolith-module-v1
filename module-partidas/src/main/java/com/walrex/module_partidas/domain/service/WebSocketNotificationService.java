package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.output.WebSocketNotificationPort;
import com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto.WebSocketNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final WebSocketNotificationPort webSocketNotificationPort;

    public Mono<String> notificarMovimientoPartida(
        Integer idNextAlmacen,
        String nameNextStore,
        Integer idOrdenIngreso,
        String codigoIngreso,
        Integer idAlmacenActual,
        Integer idOrdenIngresoActual) {

        log.info("Preparando notificación de movimiento de partida al almacén: {}", nameNextStore);

        String roomName = (idNextAlmacen != null) ? String.format("store-%d", idNextAlmacen) : "";
        String storeOut = String.format("store-%d", idAlmacenActual);

        WebSocketNotificationRequest request = WebSocketNotificationRequest.builder()
            .roomName(roomName)
            .operation("R")
            .idOrdenIngreso(idOrdenIngreso)
            .codOrdenIngreso(codigoIngreso)
            .storeOut(storeOut)
            .idOrdenIngresoOut(idOrdenIngresoActual)
            .build();

        return webSocketNotificationPort.enviarNotificacionAlmacen(request)
            .then(Mono.just(String.format("La partida ha sido movida al %s", nameNextStore)))
            .doOnSuccess(message -> log.info("Notificación enviada: {}", message))
            .doOnError(error -> log.error("Error enviando notificación: {}", error.getMessage()));
    }
}
