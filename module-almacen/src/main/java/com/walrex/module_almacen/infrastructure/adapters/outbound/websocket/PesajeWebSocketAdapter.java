package com.walrex.module_almacen.infrastructure.adapters.outbound.websocket;

import com.walrex.module_almacen.application.ports.output.PesajeNotificationPort;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.infrastructure.adapters.outbound.rabbitmq.dto.PesajeNotificacionEventDTO;
import com.walrex.module_almacen.infrastructure.config.AlmacenRabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PesajeWebSocketAdapter implements PesajeNotificationPort {

    private final RabbitTemplate rabbitTemplate;

    public PesajeWebSocketAdapter(@Qualifier("almacenRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Mono<Void> notifyWeightRegistered(PesajeDetalle pesaje) {
        PesajeNotificacionEventDTO event = PesajeNotificacionEventDTO.builder()
                .id_detordeningreso(pesaje.getId_detordeningreso())
                .cod_rollo(pesaje.getCod_rollo())
                .peso_rollo(pesaje.getPeso_rollo())
                .cnt_registrados(pesaje.getCnt_registrados())
                .completado(pesaje.getCompletado())
                .build();

        log.info("Publicando evento de pesaje a RabbitMQ para rollo: {}", pesaje.getCod_rollo());

        return Mono.fromCallable(() -> {
            rabbitTemplate.convertAndSend(
                    AlmacenRabbitMQConfig.PESAJE_EXCHANGE_NAME,
                    AlmacenRabbitMQConfig.PESAJE_ROUTING_KEY,
                    event);
            return null;
        })
        .doOnSuccess(v -> log.info("Evento pesaje publicado correctamente para rollo: {}", pesaje.getCod_rollo()))
        .doOnError(e -> log.error("Error al publicar evento pesaje en RabbitMQ: {}", e.getMessage()))
        .onErrorResume(e -> Mono.empty()) // No bloquear el flujo principal si falla RabbitMQ
        .then();
    }
}
