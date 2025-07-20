package com.walrex.notification.module_websocket.infrastructure.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "guia_devolucion.creada";
    public static final String EXCHANGE_NAME = "devoluciones.exchange";
    public static final String ROUTING_KEY = "devolucion.guia.creada";

    @Bean("gatewayGuiaDevolucionQueue")
    public Queue gatewayGuiaDevolucionQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean("gatewayGuiaDevolucionExchange")
    public TopicExchange gatewayGuiaDevolucionExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean("gatewayBinding")
    public Binding gatewayBinding(Queue gatewayGuiaDevolucionQueue, TopicExchange gatewayGuiaDevolucionExchange) {
        return BindingBuilder.bind(gatewayGuiaDevolucionQueue)
            .to(gatewayGuiaDevolucionExchange)
            .with(ROUTING_KEY);
    }

    /**
     * Configuración del MessageConverter para deserializar JSON a objetos DTO
     * Necesario para recibir mensajes JSON del módulo de almacén
     * Nombre único para evitar conflictos con otros módulos
     */
    @Bean(name = "websocketJsonMessageConverter")
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configuración del RabbitTemplate para usar el MessageConverter específico
     * Esto asegura que use Jackson2JsonMessageConverter para deserializar mensajes JSON
     * Se inyecta el ConnectionFactory automáticamente configurado por Spring Boot
     */
    @Bean(name = "websocketRabbitTemplate")
    public RabbitTemplate websocketRabbitTemplate(ConnectionFactory connectionFactory,
                                                 @Qualifier("websocketJsonMessageConverter") MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean(name = "websocketListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory websocketListenerContainerFactory(
        ConnectionFactory connectionFactory,
        @Qualifier("websocketJsonMessageConverter") MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

}
