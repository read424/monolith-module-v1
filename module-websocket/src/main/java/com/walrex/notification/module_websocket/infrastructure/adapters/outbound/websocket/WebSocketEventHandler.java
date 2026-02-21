package com.walrex.notification.module_websocket.infrastructure.adapters.outbound.websocket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventHandler {

    private final SocketIOServer socketServer;

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        String namespace = client.getNamespace().getName();
        String remoteAddress = client.getRemoteAddress().toString();

        log.info("🔗 Cliente WebSocket conectado - Session: {}, Namespace: {}, IP: {}",
            sessionId, namespace, remoteAddress);

        // Enviar mensaje de bienvenida
        client.sendEvent("welcome", Map.of(
            "message", "Conexión WebSocket establecida exitosamente",
            "sessionId", sessionId,
            "namespace", namespace,
            "timestamp", System.currentTimeMillis()));

        log.info("✅ Mensaje de bienvenida enviado al cliente {}", sessionId);
    }

    /**
     * Maneja la desconexión de clientes WebSocket
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        String namespace = client.getNamespace().getName();

        log.info("💔 Cliente WebSocket desconectado - Session: {}, Namespace: {}",
            sessionId, namespace);
    }

    @OnEvent("test_message")
    public void onTestMessage(SocketIOClient client, Object data) {
        String sessionId = client.getSessionId().toString();

        log.info("📨 Mensaje de prueba recibido de cliente {}: {}", sessionId, data);

        // Echo del mensaje de prueba
        client.sendEvent("test_response", Map.of(
            "originalMessage", data,
            "response", "Mensaje recibido correctamente",
            "sessionId", sessionId,
            "timestamp", System.currentTimeMillis()));

        log.info("✅ Respuesta de prueba enviada al cliente {}", sessionId);
    }

    /**
     * Maneja mensajes de ping/pong para mantener la conexión
     */
    @OnEvent("ping")
    public void onPing(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();

        log.debug("🏓 Ping recibido de cliente {}", sessionId);

        // Responder con pong
        client.sendEvent("pong", Map.of(
            "timestamp", System.currentTimeMillis()));
    }

    /**
     * Maneja eventos de guía de devolución (mantiene compatibilidad)
     */
    @OnEvent("guia_devolucion")
    public void onGuiaDevolucion(SocketIOClient client, Object data) {
        String sessionId = client.getSessionId().toString();

        log.info("📋 Evento de guía de devolución recibido de cliente {}: {}", sessionId, data);

        // Broadcast a todos los clientes conectados
        socketServer.getBroadcastOperations().sendEvent("guia_devolucion_update", Map.of(
            "data", data,
            "timestamp", System.currentTimeMillis()));

        log.info("📡 Evento de guía de devolución broadcasteado a todos los clientes");
    }

    /**
     * El frontend emite este evento para suscribirse a los pesajes de un artículo.
     * Payload esperado: { "id_detordeningreso": 789 }
     * El cliente queda en el room "pesaje-789" y recibirá eventos "peso_registrado".
     */
    @OnEvent("join_pesaje")
    public void onJoinPesaje(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Object idRaw = data != null ? data.get("id_detordeningreso") : null;

        if (idRaw == null) {
            log.warn("⚠️ join_pesaje recibido sin id_detordeningreso del cliente {}", sessionId);
            client.sendEvent("join_pesaje_error",
                    Map.of("error", "id_detordeningreso es requerido"));
            return;
        }

        String room = "pesaje-" + idRaw;
        client.joinRoom(room);

        log.info("✅ Cliente {} se unió al room '{}'", sessionId, room);
        client.sendEvent("join_pesaje_ok", Map.of("room", room));
    }

    /**
     * El frontend emite este evento para dejar de escuchar pesajes de un artículo.
     * Payload esperado: { "id_detordeningreso": 789 }
     */
    @OnEvent("leave_pesaje")
    public void onLeavePesaje(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Object idRaw = data != null ? data.get("id_detordeningreso") : null;

        if (idRaw == null) {
            log.warn("⚠️ leave_pesaje recibido sin id_detordeningreso del cliente {}", sessionId);
            return;
        }

        String room = "pesaje-" + idRaw;
        client.leaveRoom(room);

        log.info("🚪 Cliente {} salió del room '{}'", sessionId, room);
    }

    /**
     * Maneja errores de WebSocket
     */
    @OnEvent("error")
    public void onError(SocketIOClient client, Object error) {
        String sessionId = client.getSessionId().toString();

        log.error("❌ Error en WebSocket del cliente {}: {}", sessionId, error);

        // Notificar al cliente sobre el error
        client.sendEvent("error_response", Map.of(
            "error", "Error procesado por el servidor",
            "details", error,
            "timestamp", System.currentTimeMillis()));
    }
}
