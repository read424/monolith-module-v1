package com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq;

import com.corundumstudio.socketio.SocketIOServer;
import com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq.dto.GuiaRemisionResponseEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class GuiaDevolucionGeneradaEventListener {
    private final SocketIOServer socketServer;

    /**
     * Constructor tradicional para mayor claridad y control
     * @param socketServer Servidor Socket.IO para enviar eventos a clientes WebSocket
     */
    public GuiaDevolucionGeneradaEventListener(SocketIOServer socketServer) {
        this.socketServer = socketServer;
        log.info("🔧 [GuiaDevolucionGeneradaEventListener] Inicializado con SocketIOServer: {}", socketServer.getClass().getSimpleName());
    }

    @RabbitListener(queues = "guia_devolucion.creada", containerFactory = "websocketListenerContainerFactory")
    public Mono<Void> handleEvent(GuiaRemisionResponseEventDTO event) {
        log.info("📥 [GuiaDevolucionGeneradaEventListener] Evento recibido de RabbitMQ");
        log.info("📋 [GuiaDevolucionGeneradaEventListener] Evento: {}", event);
        log.info("🔍 [GuiaDevolucionGeneradaEventListener] Detalles del evento:");
        log.info("   - Success: {}", event.getSuccess());
        log.info("   - Message: {}", event.getMessage());

        if (event.getData() != null) {
            log.info("   - ID Orden Salida: {}", event.getData().getIdOrdenSalida());
            log.info("   - ID Comprobante: {}", event.getData().getIdComprobante());
            log.info("   - Código Comprobante: {}", event.getData().getCodigoComprobante());
        } else {
            log.warn("⚠️ [GuiaDevolucionGeneradaEventListener] Evento sin datos");
        }

        return Mono.fromCallable(() -> {
            try {
                log.info("📡 [GuiaDevolucionGeneradaEventListener] Preparando broadcast a WebSocket...");

                Map<String, Object> broadcastData = Map.of(
                    "success", event.getSuccess(),
                    "idOrdenSalida", event.getData() != null ? event.getData().getIdOrdenSalida() : null,
                    "idComprobante", event.getData() != null ? event.getData().getIdComprobante() : null,
                    "codigoComprobante", event.getData() != null ? event.getData().getCodigoComprobante() : null,
                    "message", event.getMessage()
                );

                log.info("📤 [GuiaDevolucionGeneradaEventListener] Enviando evento 'guia_devolucion' a WebSocket");
                log.info("📊 [GuiaDevolucionGeneradaEventListener] Datos a enviar: {}", broadcastData);

                socketServer.getBroadcastOperations().sendEvent("guia_devolucion", broadcastData);

                log.info("✅ [GuiaDevolucionGeneradaEventListener] Evento enviado exitosamente a WebSocket");
                log.info("🎯 [GuiaDevolucionGeneradaEventListener] Broadcast completado a todos los clientes conectados");

                return null;

            } catch (Exception e) {
                log.error("❌ [GuiaDevolucionGeneradaEventListener] Error al enviar evento a WebSocket: {}", e.getMessage(), e);
                throw e;
            }
        }).doOnSuccess(result -> {
            log.info("🎉 [GuiaDevolucionGeneradaEventListener] Procesamiento completado exitosamente");
        }).doOnError(error -> {
            log.error("💥 [GuiaDevolucionGeneradaEventListener] Error en el procesamiento: {}", error.getMessage(), error);
        }).then();
    }
}
