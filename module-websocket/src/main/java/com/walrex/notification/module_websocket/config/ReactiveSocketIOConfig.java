package com.walrex.notification.module_websocket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class ReactiveSocketIOConfig {
    @Value("${socketio.port:9093}")
    private int port;

    @Value("${socketio.max-connections:1000}")
    private int maxConnections;

    @Value("${socketio.ping-interval:25000}")
    private int pingInterval;

    @Value("${socketio.ping-timeout:60000}")
    private int pingTimeout;

    @Value("${socketio.upgrade-timeout:10000}")
    private int upgradeTimeout;

    @Value("${socketio.max-frame-payload-length:65536}")
    private int maxFramePayloadLength;

    @Value("${socketio.allow-custom-requests:false}")
    private boolean allowCustomRequests;

    @Value("${socketio.cors.origin:*}")
    private String corsOrigin;

    @Bean
    public SocketIOServer socketIOServer() {
        log.info("⚙️ Configurando WebSocket Server optimizado en puerto {}", port);
        log.info("📊 Configuración de recursos:");
        log.info("   - Máximo conexiones: {}", maxConnections);
        log.info("   - Ping interval: {}ms", pingInterval);
        log.info("   - Ping timeout: {}ms", pingTimeout);
        log.info("   - Upgrade timeout: {}ms", upgradeTimeout);

        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();

        // Configuración básica
        config.setPort(port);
        config.setOrigin(corsOrigin);

        // Configuración de recursos y límites
        config.setMaxFramePayloadLength(maxFramePayloadLength);
        config.setAllowCustomRequests(allowCustomRequests);

        // Configuración de timeouts
        config.setPingInterval(pingInterval);
        config.setPingTimeout(pingTimeout);
        config.setUpgradeTimeout(upgradeTimeout);

        // Configuración de conexiones
        config.setRandomSession(true);
        config.setAuthorizationListener(data -> {
            // Autorización básica - permitir todas las conexiones por ahora
            log.debug("🔐 Autorización de conexión desde: {}", data.getAddress());
            return true;
        });

        log.info("🔧 Configuración WebSocket completada - Puerto: {}, Origen: {}", port, corsOrigin);

        return new SocketIOServer(config);
    }

    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
        log.info("🔍 Configurando SpringAnnotationScanner para WebSocket");
        SpringAnnotationScanner scanner = new SpringAnnotationScanner(socketServer);
        log.info("✅ SpringAnnotationScanner configurado correctamente");
        return scanner;
    }

    /**
     * Bean para monitorear recursos del WebSocket
     */
    @Bean
    public WebSocketResourceMonitor webSocketResourceMonitor(SocketIOServer socketServer) {
        return new WebSocketResourceMonitor(socketServer, maxConnections);
    }

    /**
     * Monitor de recursos para WebSocket
     */
    public static class WebSocketResourceMonitor {
        private final SocketIOServer socketServer;
        private final int maxConnections;

        public WebSocketResourceMonitor(SocketIOServer socketServer, int maxConnections) {
            this.socketServer = socketServer;
            this.maxConnections = maxConnections;
        }

        public void logResourceStatus() {
            try {
                int activeConnections = socketServer.getAllClients().size();
                log.info("📊 Estado de recursos WebSocket:");
                log.info("   - Conexiones activas: {}", activeConnections);
                log.info("   - Namespaces activos: {}", socketServer.getNamespace("/").getAllClients().size());

                // Verificar límites del sistema
                long maxFileDescriptors = java.lang.management.ManagementFactory.getOperatingSystemMXBean()
                        .getAvailableProcessors();
                log.info("   - CPUs disponibles: {}", maxFileDescriptors);

            } catch (Exception e) {
                log.error("❌ Error al obtener estado de recursos: {}", e.getMessage());
            }
        }

        @Scheduled(fixedRate = 30000) // Cada 30 segundos
        public void logResourceUsage() {
            try {
                int activeConnections = socketServer.getAllClients().size();
                long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long maxMemory = Runtime.getRuntime().maxMemory();

                log.info("📊 Recursos - Conexiones: {}/{}, Memoria: {}MB/{}MB",
                        activeConnections, maxConnections,
                        memoryUsage / (1024 * 1024),
                        maxMemory / (1024 * 1024));

                // Alertas de recursos
                if (activeConnections > (maxConnections * 0.8)) {
                    log.warn("⚠️ Conexiones cercanas al límite: {}/{} (80%)", activeConnections, maxConnections);
                }

                if (memoryUsage > (maxMemory * 0.8)) {
                    log.warn("⚠️ Memoria cercana al límite: {}MB/{}MB (80%)",
                            memoryUsage / (1024 * 1024),
                            maxMemory / (1024 * 1024));
                }

            } catch (Exception e) {
                log.error("❌ Error monitoreando recursos: {}", e.getMessage());
            }
        }
    }
}
