package com.walrex.kafka.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 🔄 AOP Annotation para Kafka Request-Response Pattern
 * 
 * Automatiza:
 * - Envío de request con correlationId
 * - Escucha de response en topic específico
 * - Timeout management
 * - Cleanup automático de requests pendientes
 * - Mapeo automático de respuestas
 * 
 * @author Kafka AOP Module
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaRequestResponse {

    /**
     * Topic donde enviar la request
     */
    String requestTopic();

    /**
     * Topic donde escuchar la response
     */
    String responseTopic();

    /**
     * Clase del schema para request
     */
    Class<?> requestSchemaClass();

    /**
     * Clase del schema para response
     */
    Class<?> responseSchemaClass();

    /**
     * Timeout para la response (ms)
     */
    long timeoutMs() default 30000;

    /**
     * Campo que contiene el correlation ID en request
     */
    String correlationIdField() default "correlationId";

    /**
     * Campo que contiene el correlation ID en response
     */
    String responseCorrelationIdField() default "correlationId";

    /**
     * Group ID para el consumer de response
     */
    String responseGroupId() default "${spring.application.name:kafka-aop}-request-response";

    /**
     * Configuración de resilience para request
     */
    KafkaResilience resilience() default @KafkaResilience;

    /**
     * Habilitar cleanup automático de requests expiradas
     */
    boolean enableAutoCleanup() default true;

    /**
     * Intervalo de cleanup (ms)
     */
    long cleanupIntervalMs() default 60000;

    /**
     * Máximo número de requests pendientes
     */
    int maxPendingRequests() default 1000;

    /**
     * Habilitar métricas de request-response
     */
    boolean enableMetrics() default true;

    /**
     * Habilitar logging automático
     */
    boolean enableLogging() default true;

    /**
     * Error handler para responses
     */
    String errorHandler() default "";
} 