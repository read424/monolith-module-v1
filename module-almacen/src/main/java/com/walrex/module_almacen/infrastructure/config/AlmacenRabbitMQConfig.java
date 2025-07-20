package com.walrex.module_almacen.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlmacenRabbitMQConfig {

    public static final String QUEUE_NAME = "guia_devolucion.creada";
    public static final String EXCHANGE_NAME = "devoluciones.exchange";
    public static final String ROUTING_KEY = "devolucion.guia.creada";

    @Bean("almacenGuiaDevolucionQueue")
    public Queue almacenGuiaDevolucionQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean("almacenDevolucionesExchange")
    public TopicExchange almacenDevolucionesExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean("almacenBinding")
    public Binding almacenBinding(Queue almacenGuiaDevolucionQueue, TopicExchange almacenDevolucionesExchange) {
        return BindingBuilder.bind(almacenGuiaDevolucionQueue)
                .to(almacenDevolucionesExchange)
                .with(ROUTING_KEY);
    }

    /**
     * Configuración del MessageConverter para serializar objetos DTO a JSON
     * Soluciona el error: "SimpleMessageConverter only supports String, byte[] and
     * Serializable payloads"
     * Nombre único para evitar conflictos con otros módulos
     */
    @Bean(name = "almacenJsonMessageConverter")
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configuración del RabbitTemplate para usar el MessageConverter específico
     * Esto asegura que use Jackson2JsonMessageConverter en lugar del
     * SimpleMessageConverter por defecto
     * Se inyecta el ConnectionFactory automáticamente configurado por Spring Boot
     */
    @Bean(name = "almacenRabbitTemplate")
    public RabbitTemplate almacenRabbitTemplate(ConnectionFactory connectionFactory,
            @Qualifier("almacenJsonMessageConverter") MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
