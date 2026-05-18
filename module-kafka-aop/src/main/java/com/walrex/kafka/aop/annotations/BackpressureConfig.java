package com.walrex.kafka.aop.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 🚰 Configuración de Backpressure para Kafka
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BackpressureConfig {

    /**
     * Tamaño del buffer para backpressure
     */
    int bufferSize() default 1000;

    /**
     * Estrategia de overflow
     */
    OverflowStrategy overflowStrategy() default OverflowStrategy.BUFFER;

    /**
     * Número de elementos a prefetch
     */
    int prefetch() default 50;

    /**
     * Paralelismo para procesamiento
     */
    int parallelism() default 10;

    /**
     * Máxima concurrencia
     */
    int maxConcurrency() default 20;

    /**
     * Estrategias de overflow soportadas
     */
    enum OverflowStrategy {
        BUFFER,
        DROP_LATEST,
        DROP_OLDEST,
        ERROR
    }
} 