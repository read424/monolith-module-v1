package com.walrex.module_ecomprobantes.infrastructure.adapters.config.resilience;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ComprobantesResilienceConfig {

    // ✅ CIRCUIT BREAKERS
    @Bean(name = "comprobantesGenerationCircuitBreaker")
    public CircuitBreaker comprobantesGenerationCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("comprobante-generation");

        circuitBreaker.getEventPublisher()
                .onStateTransition(
                        event -> log.info("🔄 Circuit Breaker [comprobantes-generation] transición: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onFailureRateExceeded(
                        event -> log.warn("⚠️ Circuit Breaker [comprobantes-generation] umbral de fallas excedido: {}%",
                                event.getFailureRate()));

        return circuitBreaker;
    }

    @Bean(name = "comprobantesKafkaProducerCircuitBreaker")
    public CircuitBreaker comprobantesKafkaProducerCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("kafka-producer");

        circuitBreaker.getEventPublisher()
                .onStateTransition(
                        event -> log.info("🔄 Circuit Breaker [comprobantes-kafka-producer] transición: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    @Bean(name = "comprobantesKafkaConsumerCircuitBreaker")
    public CircuitBreaker comprobantesKafkaConsumerCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("kafka-consumer");

        circuitBreaker.getEventPublisher()
                .onStateTransition(
                        event -> log.info("🔄 Circuit Breaker [comprobantes-kafka-consumer] transición: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    // ✅ RATE LIMITERS
    @Bean(name = "comprobantesProcessingRateLimiter")
    public RateLimiter comprobantesProcessingRateLimiter(RateLimiterRegistry registry) {
        RateLimiter rateLimiter = registry.rateLimiter("comprobante-processing");

        rateLimiter.getEventPublisher()
                .onSuccess(event -> log.debug("✅ Rate Limiter [comprobantes-processing] permiso adquirido"))
                .onFailure(event -> log
                        .warn("🚫 Rate Limiter [comprobantes-processing] permiso rechazado - límite alcanzado"));

        return rateLimiter;
    }

    @Bean(name = "comprobantesKafkaEventsRateLimiter")
    public RateLimiter comprobantesKafkaEventsRateLimiter(RateLimiterRegistry registry) {
        RateLimiter rateLimiter = registry.rateLimiter("kafka-events");

        rateLimiter.getEventPublisher()
                .onSuccess(event -> log.debug("✅ Rate Limiter [comprobantes-kafka-events] permiso adquirido"))
                .onFailure(event -> log
                        .warn("🚫 Rate Limiter [comprobantes-kafka-events] permiso rechazado - límite alcanzado"));

        return rateLimiter;
    }

    // ✅ BULKHEADS
    @Bean(name = "comprobantesServiceBulkhead")
    public Bulkhead comprobantesServiceBulkhead(BulkheadRegistry registry) {
        Bulkhead bulkhead = registry.bulkhead("comprobante-service");

        bulkhead.getEventPublisher()
                .onCallPermitted(
                        event -> log.debug("✅ Bulkhead [comprobantes-service] llamada permitida - disponibles: {}",
                                bulkhead.getMetrics().getAvailableConcurrentCalls()))
                .onCallRejected(event -> log
                        .warn("🚫 Bulkhead [comprobantes-service] llamada rechazada - sin capacidad disponible"));

        return bulkhead;
    }

    @Bean(name = "comprobantesKafkaProcessingBulkhead")
    public Bulkhead comprobantesKafkaProcessingBulkhead(BulkheadRegistry registry) {
        Bulkhead bulkhead = registry.bulkhead("kafka-processing");

        bulkhead.getEventPublisher()
                .onCallPermitted(event -> log.debug(
                        "✅ Bulkhead [comprobantes-kafka-processing] llamada permitida - disponibles: {}",
                        bulkhead.getMetrics().getAvailableConcurrentCalls()))
                .onCallRejected(event -> log.warn(
                        "🚫 Bulkhead [comprobantes-kafka-processing] llamada rechazada - sin capacidad disponible"));

        return bulkhead;
    }

    // ✅ TIME LIMITERS
    @Bean(name = "comprobantesTimeoutTimeLimiter")
    public TimeLimiter comprobantesTimeoutTimeLimiter(TimeLimiterRegistry registry) {
        TimeLimiter timeLimiter = registry.timeLimiter("comprobante-timeout");

        timeLimiter.getEventPublisher()
                .onTimeout(event -> log.warn("⏰ Time Limiter [comprobantes-timeout] timeout ejecutado"));

        return timeLimiter;
    }

    @Bean(name = "comprobantesKafkaTimeoutTimeLimiter")
    public TimeLimiter comprobantesKafkaTimeoutTimeLimiter(TimeLimiterRegistry registry) {
        TimeLimiter timeLimiter = registry.timeLimiter("kafka-timeout");

        timeLimiter.getEventPublisher()
                .onTimeout(event -> log.warn("⏰ Time Limiter [comprobantes-kafka-timeout] timeout ejecutado"));

        return timeLimiter;
    }
}