package com.walrex.module_almacen.infrastructure.adapters.inbound.consumer;

import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.GuiaRemisionRemitenteResponse;
import com.walrex.module_almacen.application.ports.input.ActualizarComprobanteDevolucionUseCase;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.consumer.mapper.GuiaRemisionResponseAvroMapper;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.*;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class GuiaRemisionResponseEventConsumerAdapter {

    private final ActualizarComprobanteDevolucionUseCase actualizarComprobanteDevolucionUseCase;
    private final ReceiverOptions<String, Object> receiverOptions;
    private final CircuitBreaker consumerCircuitBreaker;
    private final RateLimiter rateLimiter;
    private final Bulkhead processingBulkhead;
    private final GuiaRemisionResponseAvroMapper guiaRemisionResponseMapper;

    @Value("${kafka.topics.almacen.response-create-guia-remision-remitente}")
    private String topicName;

    public GuiaRemisionResponseEventConsumerAdapter(
            ActualizarComprobanteDevolucionUseCase actualizarComprobanteDevolucionUseCase,
            @Qualifier("almacenModuleReceiveOptions") ReceiverOptions<String, Object> receiverOptions,
            @Qualifier("almacenKafkaConsumerCircuitBreaker") CircuitBreaker consumerCircuitBreaker,
            @Qualifier("almacenKafkaEventsRateLimiter") RateLimiter rateLimiter,
            @Qualifier("almacenKafkaProcessingBulkhead") Bulkhead processingBulkhead,
            GuiaRemisionResponseAvroMapper guiaRemisionResponseMapper) {
        this.actualizarComprobanteDevolucionUseCase = actualizarComprobanteDevolucionUseCase;
        this.receiverOptions = receiverOptions;
        this.consumerCircuitBreaker = consumerCircuitBreaker;
        this.rateLimiter = rateLimiter;
        this.processingBulkhead = processingBulkhead;
        this.guiaRemisionResponseMapper = guiaRemisionResponseMapper;
    }

    @PostConstruct
    public void startConsumer() {
        log.info("🚀 Iniciando consumer resiliente para topic: {}", topicName);

        consumeResponseGuiaRemisionEvents()
                .doOnError(error -> log.error("❌ Error fatal en consumer: {}", error.getMessage(),
                        error))
                .doOnComplete(() -> log.info("✅ Consumer completado"))
                .subscribe();
    }

    public Flux<Void> consumeResponseGuiaRemisionEvents() {
        // ✅ Configuración de ReceiverOptions con backpressure
        ReceiverOptions<String, Object> options = receiverOptions
                .subscription(Collections.singletonList(
                        topicName))
                .consumerProperty("max.poll.records",
                        50);

        return KafkaReceiver.create(options)
                .receive()
                // ✅ BACKPRESSURE BUFFER - Evita OOM en picos de tráfico
                .onBackpressureBuffer(
                        1000,
                        error -> log.error("🚫 Buffer de backpressure lleno: {}",
                                error.toString()))
                // ✅ RATE LIMITING - Controla la velocidad de procesamiento
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                // ✅ CIRCUIT BREAKER - Protege contra fallas en cascada
                .transformDeferred(CircuitBreakerOperator.of(consumerCircuitBreaker))
                // ✅ BULKHEAD - Aísla el procesamiento de eventos
                .transformDeferred(BulkheadOperator.of(processingBulkhead))
                // ✅ PARALELISMO CONTROLADO - Procesa en paralelo pero limitado
                .parallel(10)
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::processRecord)
                .sequential()
                // ✅ RETRY STRATEGY - Reintentos con backoff exponencial
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .jitter(0.1)
                        .doBeforeRetry(signal -> log.warn(
                                "🔄 Reintentando procesamiento tras error: {}",
                                signal.failure().getMessage())))
                // ✅ ERROR HANDLING - Manejo graceful de errores no recuperables
                .onErrorResume(error -> {
                    log.error("💥 Error no recuperable en consumer: {}", error.getMessage(), error);
                    // En un caso real, podrías enviar a DLQ o notificar alertas
                    return Mono.empty();
                });
    }

    private Mono<Void> processRecord(ReceiverRecord<String, Object> record) {
        String correlationId = extractCorrelationId(record);

        log.info("📨 Procesando evento - Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                correlationId);

        return Mono.defer(() -> {
            if (record.value() instanceof GuiaRemisionRemitenteResponse message) {
                log.info("🔄 Procesando guía de remisión - Success: {}, Message: {}, CorrelationId: {}",
                        message.getSuccess(), message.getMessage(), correlationId);
                GuiaRemisionResponseEventDTO guiaRemisionResponse = guiaRemisionResponseMapper.mapAvroToDto(message);
                if (guiaRemisionResponse.getSuccess()) {
                    // ✅ Procesar con el use case y confirmar offset al finalizar
                    return actualizarComprobanteDevolucionUseCase
                            .actualizarComprobanteDevolucion(
                                    guiaRemisionResponse, correlationId)
                            .doOnSuccess(v -> {
                                log.info("✅ Guía de remisión procesada exitosamente - CorrelationId: {}",
                                        correlationId);
                                record.receiverOffset().acknowledge();
                                log.debug("✅ Offset confirmado - Partition: {}, Offset: {}",
                                        record.partition(),
                                        record.offset());
                            })
                            .doOnError(error -> {
                                log.error("❌ Error procesando guía de remisión - CorrelationId: {}, Error: {}",
                                        correlationId, error.getMessage());
                                record.receiverOffset().acknowledge(); // Confirmar para evitar
                                                                       // reprocessing infinito
                            });
                }
                return Mono.empty();
            } else {
                log.warn("⚠️ Mensaje recibido no es del tipo esperado: {} - CorrelationId: {}",
                        record.value().getClass().getSimpleName(), correlationId);
                record.receiverOffset().acknowledge();
                return Mono.empty();
            }
        })
                .onErrorResume(error -> {
                    log.error("❌ Error procesando record - CorrelationId: {}, Error: {}",
                            correlationId,
                            error.getMessage());
                    record.receiverOffset().acknowledge(); // Confirmar para evitar reprocessing
                                                           // infinito
                    return Mono.empty();
                });
    }

    private String extractCorrelationId(ReceiverRecord<String, Object> record) {
        // Extraer correlation ID de headers si existe
        return record.headers().lastHeader("correlationId") != null
                ? new String(record.headers().lastHeader("correlationId").value())
                : "unknown-" + System.currentTimeMillis();
    }
}
