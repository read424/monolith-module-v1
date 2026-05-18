package com.walrex.kafka.aop.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 🔄 Configuración de Retry para Kafka
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryConfig {

    /**
     * Número máximo de reintentos
     */
    int maxAttempts() default 3;

    /**
     * Delay inicial para backoff (ms)
     */
    long backoffDelay() default 1000;

    /**
     * Multiplicador para backoff exponencial
     */
    double backoffMultiplier() default 2.0;

    /**
     * Delay máximo para backoff (ms)
     */
    long maxBackoffDelay() default 10000;

    /**
     * Jitter para randomizar backoff (0.0 - 1.0)
     */
    double jitter() default 0.1;

    /**
     * Excepciones que causan retry
     */
    Class<? extends Throwable>[] retryableExceptions() default {
        java.util.concurrent.TimeoutException.class,
        java.io.IOException.class
    };

    /**
     * Excepciones que NO causan retry
     */
    Class<? extends Throwable>[] ignoreExceptions() default {};
} 