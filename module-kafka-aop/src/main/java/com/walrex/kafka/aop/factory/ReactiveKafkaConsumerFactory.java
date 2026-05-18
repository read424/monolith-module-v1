package com.walrex.kafka.aop.factory;

import java.time.Duration;

import org.springframework.stereotype.Component;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 🏭 Factory para crear componentes reactivos de Kafka
 * 
 * Centraliza la creación de:
 * - Circuit Breakers
 * - Rate Limiters  
 * - Bulkheads
 * - Configuraciones dinámicas
 * 
 * @author Kafka AOP Module
 */
@Component
@Slf4j
public class ReactiveKafkaConsumerFactory {

    /**
     * 🔵 Crea Circuit Breaker con configuración optimizada para Kafka
     */
    public CircuitBreaker createCircuitBreaker(String name) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)                    // 50% failure rate
                .minimumNumberOfCalls(5)                        // Mínimo 5 llamadas
                .slidingWindowSize(10)                          // Ventana de 10 llamadas
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 30s en estado abierto
                .permittedNumberOfCallsInHalfOpenState(3)       // 3 llamadas en half-open
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                    org.apache.kafka.common.errors.TimeoutException.class,
                    org.apache.kafka.common.errors.NetworkException.class,
                    org.apache.kafka.common.errors.RetriableException.class,
                    java.util.concurrent.TimeoutException.class,
                    java.io.IOException.class
                )
                .ignoreExceptions(
                    java.lang.IllegalArgumentException.class,
                    java.lang.IllegalStateException.class
                )
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of(name, config);
        
        // ✅ Event listeners para logging
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.info("🔄 Circuit Breaker [{}] transición: {} -> {}", 
                        name, event.getStateTransition().getFromState(), 
                        event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("⚠️ Circuit Breaker [{}] umbral de fallas excedido: {}%", 
                        name, event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.warn("🚫 Circuit Breaker [{}] llamada no permitida", name));

        log.info("✅ Circuit Breaker [{}] creado exitosamente", name);
        return circuitBreaker;
    }

    /**
     * ⏱️ Crea Rate Limiter con configuración optimizada para Kafka
     */
    public RateLimiter createRateLimiter(String name, int limitForPeriod) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)                    // Límite por período
                .limitRefreshPeriod(Duration.ofSeconds(1))         // Refrescar cada segundo
                .timeoutDuration(Duration.ofMillis(500))           // Timeout de 500ms
                .build();

        RateLimiter rateLimiter = RateLimiter.of(name, config);
        
        // ✅ Event listeners para logging
        rateLimiter.getEventPublisher()
                .onSuccess(event -> 
                    log.debug("✅ Rate Limiter [{}] permiso adquirido", name))
                .onFailure(event -> 
                    log.warn("🚫 Rate Limiter [{}] permiso rechazado - límite alcanzado", name));

        log.info("✅ Rate Limiter [{}] creado con límite: {} req/seg", name, limitForPeriod);
        return rateLimiter;
    }

    /**
     * 🏗️ Crea Bulkhead con configuración optimizada para Kafka
     */
    public Bulkhead createBulkhead(String name, int maxConcurrentCalls) {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(maxConcurrentCalls)           // Máximo llamadas concurrentes
                .maxWaitDuration(Duration.ofMillis(1000))         // Máximo tiempo de espera
                .build();

        Bulkhead bulkhead = Bulkhead.of(name, config);
        
        // ✅ Event listeners para logging
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> 
                    log.debug("✅ Bulkhead [{}] llamada permitida - disponibles: {}", 
                        name, bulkhead.getMetrics().getAvailableConcurrentCalls()))
                .onCallRejected(event -> 
                    log.warn("🚫 Bulkhead [{}] llamada rechazada - sin capacidad disponible", name))
                .onCallFinished(event -> 
                    log.debug("🏁 Bulkhead [{}] llamada terminada - disponibles: {}", 
                        name, bulkhead.getMetrics().getAvailableConcurrentCalls()));

        log.info("✅ Bulkhead [{}] creado con límite: {} llamadas concurrentes", name, maxConcurrentCalls);
        return bulkhead;
    }

    /**
     * 📊 Crea configuración dinámica basada en métricas
     */
    public CircuitBreakerConfig createDynamicCircuitBreakerConfig(String topicName, String groupId) {
        // TODO: Implementar configuración dinámica basada en:
        // - Latencia promedio del topic
        // - Tasa de errores histórica
        // - Throughput del consumer group
        // - Métricas de infraestructura (CPU, memoria)
        
        return CircuitBreakerConfig.ofDefaults();
    }

    /**
     * 🎚️ Ajusta Rate Limiter dinámicamente
     */
    public void adjustRateLimiter(RateLimiter rateLimiter, int newLimit) {
        // TODO: Implementar ajuste dinámico basado en:
        // - Latencia del broker
        // - Carga de la aplicación
        // - Throughput actual vs deseado
        
        log.info("🎚️ Ajustando Rate Limiter [{}] a nuevo límite: {}", 
            rateLimiter.getName(), newLimit);
    }

    /**
     * 📈 Calcula configuración óptima de backpressure
     */
    public BackpressureOptimization calculateOptimalBackpressure(String topicName, String groupId) {
        // TODO: Implementar cálculo inteligente basado en:
        // - Tasa de producción del topic
        // - Velocidad de procesamiento del consumer
        // - Memoria disponible
        // - Latencia objetivo
        
        return new BackpressureOptimization(1000, 50, 10);
    }

    /**
     * 📊 Record para optimización de backpressure
     */
    public record BackpressureOptimization(
        int bufferSize,
        int prefetch, 
        int parallelism
    ) {}
} 