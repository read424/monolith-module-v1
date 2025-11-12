package com.walrex.module_almacen.infrastructure.config.resilience;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AlmacenResilienceConfing {

        // ✅ CIRCUIT BREAKERS
        @Bean(name = "almacenGenerationCircuitBreaker")
        public CircuitBreaker almacenGenerationCircuitBreaker(CircuitBreakerRegistry registry) {
                CircuitBreaker circuitBreaker = registry.circuitBreaker("almacen-generation");

                circuitBreaker.getEventPublisher()
                                .onStateTransition(
                                                event -> log.info(
                                                                "🔄 Circuit Breaker [almacen-generation] transición: {} -> {}",
                                                                event.getStateTransition().getFromState(),
                                                                event.getStateTransition().getToState()))
                                .onFailureRateExceeded(
                                                event -> log.warn(
                                                                "⚠️ Circuit Breaker [almacen-generation] umbral de fallas excedido: {}%",
                                                                event.getFailureRate()));
                return circuitBreaker;
        }

        @Bean(name = "almacenKafkaProducerCircuitBreaker")
        public CircuitBreaker almacenKafkaProducerCircuitBreaker(CircuitBreakerRegistry registry) {
                CircuitBreaker circuitBreaker = registry.circuitBreaker("almacen-kafka-producer");

                circuitBreaker.getEventPublisher()
                                .onStateTransition(
                                                event -> log.info(
                                                                "🔄 Circuit Breaker [almacen-kafka-producer] transición: {} -> {}",
                                                                event.getStateTransition().getFromState(),
                                                                event.getStateTransition().getToState()));
                return circuitBreaker;
        }

        @Bean(name = "almacenKafkaConsumerCircuitBreaker")
        public CircuitBreaker almacenKafkaConsumerCircuitBreaker(CircuitBreakerRegistry registry) {
                CircuitBreaker circuitBreaker = registry.circuitBreaker("almacen-kafka-consumer");

                circuitBreaker.getEventPublisher()
                                .onStateTransition(
                                                event -> log.info(
                                                                "🔄 Circuit Breaker [comprobantes-kafka-consumer] transición: {} -> {}",
                                                                event.getStateTransition().getFromState(),
                                                                event.getStateTransition().getToState()));

                return circuitBreaker;
        }

        // ✅ RATE LIMITERS
        @Bean(name = "almacenProcessingRateLimiter")
        public RateLimiter almacenProcessingRateLimiter(RateLimiterRegistry registry) {
                RateLimiter rateLimiter = registry.rateLimiter("almacen-events-rate-limiter");

                rateLimiter.getEventPublisher()
                                .onSuccess(event -> log.debug("✅ Rate Limiter [almacen-processing] permiso adquirido"))
                                .onFailure(event -> log.warn(
                                                "⚠️ Rate Limiter [almacen-processing] umbral de fallas excedido: {}%",
                                                event.getNumberOfPermits()));
                return rateLimiter;
        }

        @Bean(name = "almacenKafkaEventsRateLimiter")
        public RateLimiter almacenKafkaEventsRateLimiter(RateLimiterRegistry registry) {
                RateLimiter rateLimiter = registry.rateLimiter("almacen-kafka-events");

                rateLimiter.getEventPublisher()
                                .onSuccess(event -> log
                                                .debug("✅ Rate Limiter [almacen-kafka-events] permiso adquirido"))
                                .onFailure(event -> log.warn(
                                                "⚠️ Rate Limiter [almacen-kafka-events] umbral de fallas excedido: {}%",
                                                event.getNumberOfPermits()));
                return rateLimiter;
        }

        // ✅ BULKHEADS
        @Bean(name = "almacenServiceBulkhead")
        public Bulkhead almacenServiceBulkhead(BulkheadRegistry registry) {
                Bulkhead bulkhead = registry.bulkhead("almacen-service");

                bulkhead.getEventPublisher()
                                .onCallPermitted(event -> log.debug(
                                                "✅ Bulkhead [almacen-service] llamada permitida - disponibles: {}",
                                                bulkhead.getMetrics().getAvailableConcurrentCalls()))
                                .onCallRejected(event -> log.warn(
                                                "🚫 Bulkhead [comprobantes-service] llamada rechazada - sin capacidad disponible"));
                return bulkhead;
        }

        @Bean(name = "almacenKafkaProcessingBulkhead")
        public Bulkhead almacenKafkaProcessingBulkhead(BulkheadRegistry registry) {
                Bulkhead bulkhead = registry.bulkhead("almacen-kafka-processing");

                bulkhead.getEventPublisher()
                                .onCallPermitted(event -> log.debug(
                                                "✅ Bulkhead [almacen-kafka-processing] llamada permitida - disponibles: {}",
                                                bulkhead.getMetrics().getAvailableConcurrentCalls()))
                                .onCallRejected(event -> log.warn(
                                                "🚫 Bulkhead [almacen-kafka-processing] llamada rechazada - sin capacidad disponible"));
                return bulkhead;
        }
}
