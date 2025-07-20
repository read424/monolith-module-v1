package com.walrex.notification.module_websocket.config;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final SocketIOServer socketIOServer;

    @Value("${socketio.port:9093}")
    private int port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // Iniciar el servidor WebSocket
            socketIOServer.start();

            // Log de éxito cuando el WebSocket esté disponible
            log.info("🚀 WebSocket Server iniciado exitosamente en puerto {}", port);
            log.info("📡 WebSocket disponible en: http://localhost:{}/ws", port);
            log.info("🔗 Conexión WebSocket lista para recibir clientes");

        } catch (Exception e) {
            log.error("❌ Error al iniciar el servidor WebSocket en puerto {}: {}", port, e.getMessage(), e);
            throw new RuntimeException("No se pudo iniciar el servidor WebSocket", e);
        }
    }
}
