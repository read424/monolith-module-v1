package com.walrex.kafka.aop.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 🎛️ Propiedades de configuración para Kafka AOP Module
 * 
 * Permite configurar todos los aspectos del módulo AOP:
 * - Habilitación del módulo
 * - Configuraciones por defecto
 * - Modo desarrollo y testing  
 * - Métricas y monitoreo
 * - Optimizaciones de performance
 * 
 * @author Kafka AOP Module
 */
@ConfigurationProperties(prefix = "kafka-aop")
@Data
public class KafkaAopProperties {

    /**
     * Habilitar/deshabilitar el módulo AOP completo
     */
    private boolean enabled = true;

    /**
     * Configuraciones por defecto para consumers
     */
    private Consumer consumer = new Consumer();

    /**
     * Configuraciones por defecto para producers
     */
    private Producer producer = new Producer();

    /**
     * Configuraciones de resilience por defecto
     */
    private Resilience resilience = new Resilience();

    /**
     * Configuraciones de métricas
     */
    private Metrics metrics = new Metrics();

    /**
     * Configuraciones de modo desarrollo
     */
    private DevMode devMode = new DevMode();

    /**
     * Configuraciones de Schema Registry
     */
    private SchemaRegistry schemaRegistry = new SchemaRegistry();

    /**
     * Configuraciones de optimización
     */
    private Optimization optimization = new Optimization();

    @Data
    public static class Consumer {
        /**
         * Configuración por defecto de backpressure
         */
        private Backpressure backpressure = new Backpressure();

        /**
         * Timeout por defecto para procesamiento (ms)
         */
        private long defaultProcessingTimeoutMs = 30000;

        /**
         * Auto-offset reset por defecto
         */
        private String defaultAutoOffsetReset = "latest";

        /**
         * Habilitar logging automático por defecto
         */
        private boolean defaultLoggingEnabled = true;

        /**
         * Habilitar métricas automáticas por defecto
         */
        private boolean defaultMetricsEnabled = true;

        @Data
        public static class Backpressure {
            private int defaultBufferSize = 1000;
            private int defaultPrefetch = 50;
            private int defaultParallelism = 10;
            private int defaultMaxConcurrency = 20;
        }
    }

    @Data
    public static class Producer {
        /**
         * Acknowledgment level por defecto
         */
        private String defaultAcks = "all";

        /**
         * Habilitar idempotencia por defecto
         */
        private boolean defaultIdempotenceEnabled = true;

        /**
         * Timeout de delivery por defecto (ms)
         */
        private long defaultDeliveryTimeoutMs = 120000;

        /**
         * Tipo de compresión por defecto
         */
        private String defaultCompressionType = "snappy";

        /**
         * Headers automáticos por defecto
         */
        private String[] defaultAutoHeaders = {"correlationId", "timestamp", "source", "messageId"};
    }

    @Data
    public static class Resilience {
        /**
         * Circuit Breaker por defecto
         */
        private CircuitBreaker circuitBreaker = new CircuitBreaker();

        /**
         * Rate Limiter por defecto
         */
        private RateLimiter rateLimiter = new RateLimiter();

        /**
         * Bulkhead por defecto
         */
        private Bulkhead bulkhead = new Bulkhead();

        /**
         * Retry por defecto
         */
        private Retry retry = new Retry();

        @Data
        public static class CircuitBreaker {
            private boolean defaultEnabled = true;
            private float defaultFailureRateThreshold = 50.0f;
            private int defaultMinimumNumberOfCalls = 5;
            private int defaultSlidingWindowSize = 10;
            private Duration defaultWaitDurationInOpenState = Duration.ofSeconds(30);
        }

        @Data
        public static class RateLimiter {
            private boolean defaultEnabled = true;
            private int defaultLimitForPeriod = 100;
            private Duration defaultLimitRefreshPeriod = Duration.ofSeconds(1);
            private Duration defaultTimeoutDuration = Duration.ofMillis(500);
        }

        @Data
        public static class Bulkhead {
            private boolean defaultEnabled = true;
            private int defaultMaxConcurrentCalls = 10;
            private Duration defaultMaxWaitDuration = Duration.ofMillis(1000);
        }

        @Data
        public static class Retry {
            private int defaultMaxAttempts = 3;
            private long defaultBackoffDelay = 1000;
            private double defaultBackoffMultiplier = 2.0;
            private long defaultMaxBackoffDelay = 10000;
            private double defaultJitter = 0.1;
        }
    }

    @Data
    public static class Metrics {
        /**
         * Habilitar métricas automáticas
         */
        private boolean enabled = true;

        /**
         * Configuración de Prometheus
         */
        private Prometheus prometheus = new Prometheus();

        /**
         * Cleanup automático de métricas
         */
        private Cleanup cleanup = new Cleanup();

        @Data
        public static class Prometheus {
            private boolean enabled = false;
            private String prefix = "kafka_aop";
            private boolean includeHostTag = true;
            private boolean includeApplicationTag = true;
        }

        @Data
        public static class Cleanup {
            private boolean enabled = true;
            private Duration maxAge = Duration.ofHours(24);
            private Duration interval = Duration.ofHours(1);
        }
    }

    @Data
    public static class DevMode {
        /**
         * Habilitar modo desarrollo
         */
        private boolean enabled = false;

        /**
         * Logging verbose en desarrollo
         */
        private boolean verboseLogging = true;

        /**
         * Mock de Schema Registry en desarrollo
         */
        private boolean mockSchemaRegistry = false;

        /**
         * Embedded Kafka en testing
         */
        private boolean embeddedKafka = false;

        /**
         * Validaciones extra en desarrollo
         */
        private boolean extraValidations = true;
    }

    @Data
    public static class SchemaRegistry {
        /**
         * URL por defecto
         */
        private String defaultUrl = "http://localhost:8081";

        /**
         * Cache de configuraciones
         */
        private Cache cache = new Cache();

        /**
         * Autenticación
         */
        private Auth auth = new Auth();

        @Data
        public static class Cache {
            private boolean enabled = true;
            private int maxSize = 100;
            private Duration expireAfterWrite = Duration.ofMinutes(30);
        }

        @Data
        public static class Auth {
            private boolean enabled = false;
            private String username = "";
            private String password = "";
        }
    }

    @Data
    public static class Optimization {
        /**
         * Optimizaciones automáticas
         */
        private boolean autoOptimization = false;

        /**
         * Ajuste dinámico de configuraciones
         */
        private boolean dynamicAdjustment = false;

        /**
         * Monitoreo de performance
         */
        private boolean performanceMonitoring = true;

        /**
         * Ajuste basado en métricas
         */
        private boolean metricsBasedTuning = false;
    }
} 