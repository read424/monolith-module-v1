package com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq;

import com.corundumstudio.socketio.SocketIOServer;
import com.walrex.notification.module_websocket.infrastructure.adapters.inbound.rabbitmq.dto.PesajeNotificacionEventDTO;
import com.walrex.notification.module_websocket.infrastructure.config.rabbitmq.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PesajePesoRegistradoEventListener {

    private static final String ROOM_PREFIX = "pesaje-";
    private static final String SOCKET_EVENT = "peso_registrado";

    private final SocketIOServer socketServer;

    public PesajePesoRegistradoEventListener(SocketIOServer socketServer) {
        this.socketServer = socketServer;
        log.info("🔧 [PesajePesoRegistradoEventListener] Inicializado, escuchando queue: {}",
                RabbitMQConfig.PESAJE_QUEUE_NAME);
    }

    @RabbitListener(queues = RabbitMQConfig.PESAJE_QUEUE_NAME,
                    containerFactory = "websocketListenerContainerFactory")
    public void handleEvent(PesajeNotificacionEventDTO event) {
        log.info("📥 [PesajePesoRegistradoEventListener] Evento recibido - rollo: {}, id_det: {}",
                event.getCod_rollo(), event.getId_detordeningreso());

        String room = ROOM_PREFIX + event.getId_detordeningreso();

        Map<String, Object> payload = Map.of(
                "id_detordeningreso", event.getId_detordeningreso(),
                "cod_rollo",          event.getCod_rollo(),
                "peso_rollo",         event.getPeso_rollo(),
                "cnt_registrados",    event.getCnt_registrados(),
                "completado",         Boolean.TRUE.equals(event.getCompletado())
        );

        socketServer.getRoomOperations(room).sendEvent(SOCKET_EVENT, payload);

        log.info("📡 [PesajePesoRegistradoEventListener] Evento '{}' emitido al room '{}' → {} clientes",
                SOCKET_EVENT, room,
                socketServer.getRoomOperations(room).getClients().size());
    }
}
