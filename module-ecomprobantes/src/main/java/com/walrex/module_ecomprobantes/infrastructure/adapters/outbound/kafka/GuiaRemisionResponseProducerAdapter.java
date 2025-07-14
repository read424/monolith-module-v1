package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.kafka;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.GuiaRemisionRemitenteResponse;
import com.walrex.module_ecomprobantes.application.ports.output.EnviarRespuestaGuiaRemisionPort;
import com.walrex.module_ecomprobantes.infrastructure.adapters.config.properties.ComprobantesKafkaProperties;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.*;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class GuiaRemisionResponseProducerAdapter implements EnviarRespuestaGuiaRemisionPort {

    private final ComprobantesKafkaProperties properties;
    private final KafkaSender<String, Object> kafkaSender;
    private final CircuitBreaker producerCircuitBreaker;
    private final RateLimiter rateLimiter;

    public GuiaRemisionResponseProducerAdapter(
            ComprobantesKafkaProperties properties,
            @Qualifier("comprobanteCreateAvroSender") KafkaSender<String, Object> kafkaSender,
            @Qualifier("comprobantesKafkaProducerCircuitBreaker") CircuitBreaker producerCircuitBreaker,
            @Qualifier("comprobantesProcessingRateLimiter") RateLimiter rateLimiter) {
        this.properties = properties;
        this.kafkaSender = kafkaSender;
        this.producerCircuitBreaker = producerCircuitBreaker;
        this.rateLimiter = rateLimiter;
    }

    private static final String RESPONSE_TOPIC = "response-create-comprobante-grr";

    @Override
    public Mono<Void> enviarRespuesta(GuiaRemisionRemitenteResponse response, String correlationId) {
        return sendGuiaRemisionResponse(response, correlationId);
    }

    /**
     * Envía respuesta de creación de guía de remisión con resiliencia completa
     */
    public Mono<Void> sendGuiaRemisionResponse(GuiaRemisionRemitenteResponse response, String correlationId) {
        log.info("📤 Enviando respuesta de guía de remisión - CorrelationId: {}, Success: {}",
                correlationId, response.getSuccess());

        return createSenderRecord(response, correlationId)
                // ✅ RATE LIMITING - Controla velocidad de envío
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                // ✅ CIRCUIT BREAKER - Protege contra fallas del broker
                .transformDeferred(CircuitBreakerOperator.of(producerCircuitBreaker))
                // ✅ ENVÍO ASÍNCRONO
                .flatMap(this::sendRecord)
                // ✅ RETRY STRATEGY - Reintentos con backoff
                .retryWhen(Retry.backoff(
                        properties.getKafka().getProducer().getRetry().getMaxAttempts(),
                        properties.getKafka().getProducer().getRetry().getBackoffDelay())
                        .maxBackoff(Duration.ofSeconds(10))
                        .jitter(0.1)
                        .filter(this::isRetriableException)
                        .doBeforeRetry(
                                signal -> log.warn("🔄 Reintentando envío - CorrelationId: {}, Intento: {}, Error: {}",
                                        correlationId, signal.totalRetries() + 1, signal.failure().getMessage())))
                // ✅ TIMEOUT PROTECTION
                .timeout(Duration.ofSeconds(30))
                // ✅ ERROR HANDLING
                .onErrorResume(error -> {
                    log.error("❌ Error final enviando respuesta - CorrelationId: {}, Error: {}",
                            correlationId, error.getMessage(), error);

                    // En producción podrías:
                    // 1. Guardar en tabla de fallos
                    // 2. Enviar a Dead Letter Queue
                    // 3. Notificar alertas

                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Crea el record de Kafka con headers y metadata
     */
    private Mono<SenderRecord<String, Object, String>> createSenderRecord(
            GuiaRemisionRemitenteResponse response, String correlationId) {

        return Mono.fromCallable(() -> {
            String messageId = UUID.randomUUID().toString();

            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                    RESPONSE_TOPIC,
                    correlationId, // Key para particionado
                    response // Value (mensaje Avro)
            );

            // ✅ HEADERS INFORMATIVOS
            producerRecord.headers()
                    .add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8))
                    .add("messageId", messageId.getBytes(StandardCharsets.UTF_8))
                    .add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8))
                    .add("source", "module-ecomprobantes".getBytes(StandardCharsets.UTF_8))
                    .add("event-type", "guia-remision-response".getBytes(StandardCharsets.UTF_8));

            return SenderRecord.create(producerRecord, correlationId);
        });
    }

    /**
     * Envía el record y maneja el resultado
     */
    private Mono<Void> sendRecord(SenderRecord<String, Object, String> senderRecord) {
        return kafkaSender.send(Mono.just(senderRecord))
                .next()
                .flatMap(this::handleSenderResult)
                .then();
    }

    /**
     * Maneja el resultado del envío
     */
    private Mono<SenderResult<String>> handleSenderResult(SenderResult<String> result) {
        if (result.exception() != null) {
            log.error("❌ Error enviando mensaje - CorrelationId: {}, Topic: {}, Partition: {}, Error: {}",
                    result.correlationMetadata(),
                    result.recordMetadata() != null ? result.recordMetadata().topic() : "unknown",
                    result.recordMetadata() != null ? result.recordMetadata().partition() : -1,
                    result.exception().getMessage());

            return Mono.error(result.exception());
        } else {
            log.info("✅ Mensaje enviado exitosamente - CorrelationId: {}, Topic: {}, Partition: {}, Offset: {}",
                    result.correlationMetadata(),
                    result.recordMetadata().topic(),
                    result.recordMetadata().partition(),
                    result.recordMetadata().offset());

            return Mono.just(result);
        }
    }

    /**
     * Determina si una excepción es recuperable para retry
     */
    private boolean isRetriableException(Throwable throwable) {
        return throwable instanceof org.apache.kafka.common.errors.RetriableException ||
                throwable instanceof java.util.concurrent.TimeoutException ||
                throwable instanceof org.apache.kafka.common.errors.NetworkException ||
                throwable instanceof org.apache.kafka.common.errors.TimeoutException;
    }

    /**
     * Método de conveniencia para envío rápido con correlationId generado
     */
    public Mono<Void> sendQuickResponse(boolean success, String message) {
        String correlationId = "quick-" + System.currentTimeMillis();

        GuiaRemisionRemitenteResponse response = GuiaRemisionRemitenteResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .setData(null)
                .build();

        return sendGuiaRemisionResponse(response, correlationId);
    }

    /**
     * Método para envío batch (futuro uso)
     */
    public Mono<Void> sendBatchResponses(java.util.List<GuiaRemisionRemitenteResponse> responses) {
        return reactor.core.publisher.Flux.fromIterable(responses)
                .index()
                .flatMap(tuple -> {
                    String correlationId = "batch-" + tuple.getT1() + "-" + System.currentTimeMillis();
                    return sendGuiaRemisionResponse(tuple.getT2(), correlationId);
                })
                .then();
    }
}