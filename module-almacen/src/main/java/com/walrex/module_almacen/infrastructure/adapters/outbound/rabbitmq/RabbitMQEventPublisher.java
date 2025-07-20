package com.walrex.module_almacen.infrastructure.adapters.outbound.rabbitmq;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.walrex.module_almacen.application.ports.output.EventPublisherOutputPort;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;
import com.walrex.module_almacen.infrastructure.config.AlmacenRabbitMQConfig;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RabbitMQEventPublisher implements EventPublisherOutputPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(@Qualifier("almacenRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        log.info("🔧 [RabbitMQEventPublisher] Inicializado con RabbitTemplate: {}",
                rabbitTemplate.getClass().getSimpleName());
    }

    @Override
    public Mono<Void> publishGuiaDevolucionEvent(GuiaRemisionResponseEventDTO event) {
        log.info("🚀 [RabbitMQEventPublisher] Iniciando publicación de evento de devolución");
        log.info("📋 [RabbitMQEventPublisher] Evento a publicar: {}", event);
        log.info("📤 [RabbitMQEventPublisher] Exchange: {}", AlmacenRabbitMQConfig.EXCHANGE_NAME);
        log.info("🔑 [RabbitMQEventPublisher] Routing Key: {}", AlmacenRabbitMQConfig.ROUTING_KEY);

        return Mono.fromCallable(() -> {
            try {
                log.info("📨 [RabbitMQEventPublisher] Enviando mensaje a RabbitMQ...");

                // Crear MessagePostProcessor para logging del mensaje serializado
                MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) {
                        try {
                            // Obtener el cuerpo del mensaje serializado
                            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

                            log.info("🔍 [RabbitMQEventPublisher] Mensaje serializado enviado a RabbitMQ:");
                            log.info("   📄 Contenido JSON: {}", messageBody);
                            log.info("   📏 Tamaño del mensaje: {} bytes", message.getBody().length);
                            log.info("   🏷️ Content-Type: {}", message.getMessageProperties().getContentType());
                            log.info("   🆔 Message ID: {}", message.getMessageProperties().getMessageId());
                            log.info("   ⏰ Timestamp: {}", message.getMessageProperties().getTimestamp());
                            log.info("   🔄 Delivery Mode: {}", message.getMessageProperties().getDeliveryMode());
                            log.info("   🎯 Exchange: {}", message.getMessageProperties().getReceivedExchange());
                            log.info("   🔑 Routing Key: {}", message.getMessageProperties().getReceivedRoutingKey());

                            // Log de headers si existen
                            if (message.getMessageProperties().getHeaders() != null
                                    && !message.getMessageProperties().getHeaders().isEmpty()) {
                                log.info("   📋 Headers: {}", message.getMessageProperties().getHeaders());
                            }

                        } catch (Exception e) {
                            log.error("❌ [RabbitMQEventPublisher] Error al procesar mensaje para logging: {}",
                                    e.getMessage(), e);
                        }

                        return message;
                    }
                };

                rabbitTemplate.convertAndSend(
                        AlmacenRabbitMQConfig.EXCHANGE_NAME,
                        AlmacenRabbitMQConfig.ROUTING_KEY,
                        event,
                        messagePostProcessor);

                log.info("✅ [RabbitMQEventPublisher] Mensaje enviado exitosamente a RabbitMQ");
                log.info("📊 [RabbitMQEventPublisher] Detalles del envío:");
                log.info("   - Exchange: {}", AlmacenRabbitMQConfig.EXCHANGE_NAME);
                log.info("   - Routing Key: {}", AlmacenRabbitMQConfig.ROUTING_KEY);
                log.info("   - Queue destino: {}", AlmacenRabbitMQConfig.QUEUE_NAME);
                log.info("   - Evento ID: {}", event.getData() != null ? event.getData().getIdOrdenSalida() : "N/A");
                log.info("   - Success: {}", event.getSuccess());
                log.info("   - Message: {}", event.getMessage());

                return event;

            } catch (Exception e) {
                log.error("❌ [RabbitMQEventPublisher] Error al enviar mensaje a RabbitMQ: {}", e.getMessage(), e);
                throw e;
            }
        }).doOnSuccess(result -> {
            log.info("🎉 [RabbitMQEventPublisher] Operación completada exitosamente");
        }).doOnError(error -> {
            log.error("💥 [RabbitMQEventPublisher] Error en la operación: {}", error.getMessage(), error);
        }).then();
    }
}
