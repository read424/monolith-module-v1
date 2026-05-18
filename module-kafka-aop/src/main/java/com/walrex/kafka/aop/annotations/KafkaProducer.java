package com.walrex.kafka.aop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 📤 AOP Annotation para Kafka Producer Reactivo
 * 
 * Automatiza:
 * - Configuración de SenderOptions
 * - Serialización Avro automática
 * - Headers automáticos (correlationId, timestamp, source)
 * - Patterns de resilience (CB, Rate Limit)
 * - Métricas de throughput
 * - Retry con backoff exponencial
 * 
 * @author Kafka AOP Module
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaProducer {

    /**
     * Topic de Kafka donde enviar
     */
    String topic();

    /**
     * Clase del schema Avro para serialización automática
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
     * Configuración de retry específica para producer
     */
    RetryConfig retry() default @RetryConfig;

    /**
     * Acknowledgment level (all, 1, 0)
     */
    String acks() default "all";

    /**
     * Habilitar idempotencia
     */
    boolean enableIdempotence() default true;

    /**
     * Timeout para envío (ms)
     */
    long deliveryTimeoutMs() default 120000;

    /**
     * Timeout para request (ms)
     */
    long requestTimeoutMs() default 30000;

    /**
     * Tipo de compresión (none, gzip, snappy, lz4, zstd)
     */
    String compressionType() default "snappy";

    /**
     * Tamaño de lote
     */
    int batchSize() default 16384;

    /**
     * Tiempo de espera para batching (ms)
     */
    long lingerMs() default 0;

    /**
     * Buffer memory para producer
     */
    long bufferMemory() default 33554432;

    /**
     * Habilitar métricas automáticas
     */
    boolean enableMetrics() default true;

    /**
     * Habilitar logging estructurado automático
     */
    boolean enableLogging() default true;

    /**
     * Headers automáticos a agregar
     */
    String[] autoHeaders() default {"correlationId", "timestamp", "source", "messageId"};

    /**
     * Configuración personalizada de properties (clave=valor)
     */
    String[] customProperties() default {};

    /**
     * Key extractor para particionado
     * Se puede usar SpEL: "#message.getId()" o método Java
     */
    String keyExtractor() default "";
} 