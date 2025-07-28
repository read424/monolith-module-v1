package com.walrex.module_ecomprobantes.infrastructure.adapters.config;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.netty.handler.timeout.TimeoutException;

/**
 * Configuración de Resilience4j para patrones de resiliencia del módulo
 * ecomprobantes.
 * Incluye Circuit Breaker, Rate Limiter, Bulkhead y Time Limiter.
 * 
 * IMPORTANTE: Los nombres de beans incluyen el prefijo 'ecomprobantes' para
 * evitar
 * conflictos en el monolito modular.
 */
@Configuration
public class EcomprobantesResilience4jConfig {

    /**
     * Configuración del Circuit Breaker para el servicio Lycet del módulo
     * ecomprobantes.
     * 
     * @return CircuitBreakerConfig configurado
     */
    @Bean("ecomprobantesLycetCircuitBreakerConfig")
    public CircuitBreakerConfig lycetCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .failureRateThreshold(50)
                .recordExceptions(
                        WebClientResponseException.class,
                        ConnectException.class,
                        SocketTimeoutException.class,
                        TimeoutException.class)
                .build();
    }

    /**
     * Configuración del Time Limiter para el servicio Lycet del módulo
     * ecomprobantes.
     * 
     * @return TimeLimiterConfig configurado
     */
    @Bean("ecomprobantesLycetTimeLimiterConfig")
    public TimeLimiterConfig lycetTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .cancelRunningFuture(true)
                .build();
    }
}