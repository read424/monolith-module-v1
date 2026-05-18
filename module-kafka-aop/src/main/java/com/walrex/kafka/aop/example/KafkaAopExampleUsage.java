package com.walrex.kafka.aop.example;

import org.springframework.stereotype.Component;

import com.walrex.kafka.aop.annotations.KafkaConsumer;
import com.walrex.kafka.aop.annotations.KafkaProducer;
import com.walrex.kafka.aop.annotations.KafkaRequestResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 📚 EJEMPLO DE USO - Kafka AOP Module
 * 
 * Muestra cómo usar las annotations del AOP para eliminar
 * toda la configuración manual de Kafka.
 * 
 * ✅ ANTES (sin AOP): 50+ líneas de configuración manual
 * ✅ DESPUÉS (con AOP): Solo annotations + lógica de negocio
 * 
 * @author Kafka AOP Module
 */
@Component
@Slf4j
public class KafkaAopExampleUsage {

    // ========================================
    // 📥 EJEMPLO: KAFKA CONSUMER CON AOP
    // ========================================

    /**
     * 🎯 CONSUMER AUTOMÁTICO
     * 
     * El AOP maneja automáticamente:
     * - Configuración de ReceiverOptions
     * - Deserialización Avro
     * - Circuit Breaker, Rate Limiter, Bulkhead
     * - Backpressure (buffer 1000, parallelism 10)
     * - Retry con backoff exponencial
     * - Logging y métricas automáticos
     * - Error handling y DLQ
     */
    @KafkaConsumer(
        topic = "user-events",
        groupId = "user-service-group",
        schemaClass = UserEvent.class, // Clase Avro generada
        schemaRegistryUrl = "${app.schema-registry.url}"
    )
    public Mono<Void> processUserEvent(UserEvent userEvent) {
        // ✅ SOLO LÓGICA DE NEGOCIO - AOP maneja todo lo demás
        log.info("🔄 Procesando evento de usuario: {}", userEvent.getUserId());
        
        return Mono.fromRunnable(() -> {
            // Tu lógica de negocio aquí
            processBusinessLogic(userEvent);
        });
    }

    // ========================================
    // 📤 EJEMPLO: KAFKA PRODUCER CON AOP  
    // ========================================

    /**
     * 🚀 PRODUCER AUTOMÁTICO
     * 
     * El AOP maneja automáticamente:
     * - Configuración de SenderOptions
     * - Serialización Avro
     * - Headers (correlationId, timestamp, source)
     * - Circuit Breaker y Rate Limiter
     * - Retry con backoff
     * - Métricas de throughput
     * - Idempotencia y acknowledgments
     */
    @KafkaProducer(
        topic = "user-notifications",
        schemaClass = UserNotification.class,
        acks = "all",
        enableIdempotence = true,
        compressionType = "snappy"
    )
    public Mono<Void> sendUserNotification(UserNotification notification) {
        // ✅ SOLO LÓGICA DE NEGOCIO - AOP maneja envío automático
        log.info("📤 Enviando notificación a usuario: {}", notification.getUserId());
        
        // AOP detecta el retorno y envía automáticamente a Kafka
        return Mono.just(notification).then();
    }

    // ========================================
    // 🔄 EJEMPLO: REQUEST-RESPONSE PATTERN
    // ========================================

    /**
     * 💬 REQUEST-RESPONSE AUTOMÁTICO
     * 
     * El AOP maneja automáticamente:
     * - Envío de request con correlationId único
     * - Escucha de response en topic específico
     * - Timeout management (30 segundos)
     * - Cleanup de requests pendientes
     * - Mapeo automático de respuesta
     */
    @KafkaRequestResponse(
        requestTopic = "user-validation-request",
        responseTopic = "user-validation-response",  
        requestSchemaClass = UserValidationRequest.class,
        responseSchemaClass = UserValidationResponse.class,
        timeoutMs = 30000
    )
    public Mono<UserValidationResponse> validateUser(UserValidationRequest request) {
        // ✅ AOP maneja todo el patrón request-response automáticamente
        log.info("🔍 Validando usuario: {}", request.getUserId());
        
        // El método puede enfocarse solo en crear la request
        // AOP maneja envío, correlación y respuesta automáticamente
        return Mono.just(request)
            .map(req -> {
                // Preparar request si es necesario
                req.setTimestamp(System.currentTimeMillis());
                return req;
            })
            .cast(UserValidationResponse.class); // AOP manejará la conversión real
    }

    // ========================================
    // 🎛️ CONFIGURACIÓN AVANZADA CON AOP
    // ========================================

    /**
     * ⚙️ CONFIGURACIÓN PERSONALIZADA
     * 
     * Ejemplo con todas las configuraciones personalizadas
     */
    @KafkaConsumer(
        topic = "complex-events",
        groupId = "complex-service-group", 
        schemaClass = ComplexEvent.class,
        resilience = @com.walrex.kafka.aop.annotations.KafkaResilience(
            circuitBreaker = "custom-cb",
            rateLimit = 50,
            bulkhead = 5,
            enableCircuitBreaker = true,
            enableRateLimiter = true,
            retry = @com.walrex.kafka.aop.annotations.RetryConfig(
                maxAttempts = 5,
                backoffDelay = 2000,
                maxBackoffDelay = 30000
            )
        ),
        backpressure = @com.walrex.kafka.aop.annotations.BackpressureConfig(
            bufferSize = 2000,
            parallelism = 20,
            maxConcurrency = 50
        ),
        processingTimeoutMs = 60000,
        enableMetrics = true,
        enableLogging = true,
        dlqTopic = "complex-events-dlq",
        customProperties = {
            "session.timeout.ms=45000",
            "heartbeat.interval.ms=15000"
        }
    )
    public Mono<Void> processComplexEvent(ComplexEvent event) {
        log.info("🔧 Procesando evento complejo: {}", event.getEventId());
        
        return Mono.fromRunnable(() -> {
            // Lógica compleja de negocio
            handleComplexBusinessLogic(event);
        });
    }

    // ========================================
    // 🔧 MÉTODOS HELPER (SIMULACIÓN)
    // ========================================

    private void processBusinessLogic(UserEvent userEvent) {
        // Simular lógica de negocio
        log.debug("Procesando lógica para usuario: {}", userEvent.getUserId());
    }

    private void handleComplexBusinessLogic(ComplexEvent event) {
        // Simular lógica compleja
        log.debug("Procesando evento complejo: {}", event.getEventId());
    }

    // ========================================
    // 📋 CLASES AVRO SIMULADAS (para ejemplo)
    // ========================================

    // Estas serían las clases reales generadas por Avro
    public static class UserEvent {
        private String userId;
        private String eventType;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class UserNotification {
        private String userId;
        private String message;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class UserValidationRequest {
        private String userId;
        private long timestamp;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class UserValidationResponse {
        private String userId;
        private boolean valid;
        private String reason;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ComplexEvent {
        private String eventId;
        private String payload;
        
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
    }
} 