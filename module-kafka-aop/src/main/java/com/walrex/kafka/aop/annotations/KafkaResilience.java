package com.walrex.kafka.aop.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 🛡️ Configuración de Resilience para Kafka
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaResilience {

    /**
     * Nombre del circuit breaker (se auto-genera si está vacío)
     */
    String circuitBreaker() default "";

    /**
     * Límite de rate limiting (requests por segundo)
     */
    int rateLimit() default 100;

    /**
     * Configuración de bulkhead (máximas llamadas concurrentes)
     */
    int bulkhead() default 10;

    /**
     * Configuración de retry
     */
    RetryConfig retry() default @RetryConfig;

    /**
     * Habilitar circuit breaker
     */
    boolean enableCircuitBreaker() default true;

    /**
     * Habilitar rate limiter
     */
    boolean enableRateLimiter() default true;

    /**
     * Habilitar bulkhead
     */
    boolean enableBulkhead() default true;
} 