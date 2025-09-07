package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto.WebSocketNotificationRequest;
import reactor.core.publisher.Mono;

public interface WebSocketNotificationPort {
    /**
     * Envía una notificación WebSocket a un almacén específico
     *
     * @param request Datos de la notificación
     * @return Mono<Void> que completa cuando la notificación se envía exitosamente
     */
    Mono<Void> enviarNotificacionAlmacen(WebSocketNotificationRequest request);
}
