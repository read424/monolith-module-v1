package com.walrex.kafka.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 🎭 AOP Annotation para Kafka Consumer Reactivo
 * 
 * Automatiza:
 * - Configuración de ReceiverOptions
 * - Deserialización Avro automática  
 * - Patterns de resilience (CB, Rate Limit, Bulkhead)
 * - Backpressure y buffering
 * - Métricas y logging automático
 * - Error handling y DLQ
 * 
 * @author Kafka AOP Module
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaConsumer {

    /**
     * Topic de Kafka a consumir
     */
    String topic();

    /**
     * Group ID del consumer
     */
    String groupId();

    /**
     * Clase del schema Avro para deserialización automática
     */
    Class<?> schemaClass();

    /**
     * URL del Schema Registry (opcional, por defecto toma de configuración)
     */
    String schemaRegistryUrl() default "${spring.kafka.producer.properties.schema.registry.url}";

    /**
     * Configuración de resilience
     */
    KafkaResilience resilience() default @KafkaResilience;

    /**
     * Configuración de backpressure
     */
    BackpressureConfig backpressure() default @BackpressureConfig;

    /**
     * Auto-offset reset strategy
     */
    String autoOffsetReset() default "latest";

    /**
     * Habilitar deserialización específica para Avro
     */
    boolean specificAvroReader() default true;

    /**
     * Tiempo máximo de procesamiento por mensaje (ms)
     */
    long processingTimeoutMs() default 30000;

    /**
     * Habilitar métricas automáticas
     */
    boolean enableMetrics() default true;

    /**
     * Habilitar logging estructurado automático
     */
    boolean enableLogging() default true;

    /**
     * Dead Letter Queue topic (opcional)
     */
    String dlqTopic() default "";

    /**
     * Configuración personalizada de properties (clave=valor)
     */
    String[] customProperties() default {};
} 