package com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.kafka;

import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.GuiaRemisionRemitenteMessage;
import com.walrex.module_ecomprobantes.infrastructure.adapters.config.properties.ComprobantesKafkaProperties;

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
public class GuiaRemisionEventConsumerAdapter {

    private final ComprobantesKafkaProperties properties;
    private final ReceiverOptions<String, Object> receiverOptions;
    private final CircuitBreaker consumerCircuitBreaker;
    private final RateLimiter rateLimiter;
    private final Bulkhead processingBulkhead;

    public GuiaRemisionEventConsumerAdapter(
            ComprobantesKafkaProperties properties,
            @Qualifier("comprobanteModuleReceiveOptions") ReceiverOptions<String, Object> receiverOptions,
            @Qualifier("comprobantesKafkaConsumerCircuitBreaker") CircuitBreaker consumerCircuitBreaker,
            @Qualifier("comprobantesKafkaEventsRateLimiter") RateLimiter rateLimiter,
            @Qualifier("comprobantesKafkaProcessingBulkhead") Bulkhead processingBulkhead) {
        this.properties = properties;
        this.receiverOptions = receiverOptions;
        this.consumerCircuitBreaker = consumerCircuitBreaker;
        this.rateLimiter = rateLimiter;
        this.processingBulkhead = processingBulkhead;
    }

    private static final String TOPIC_NAME = "create-comprobante-guia-remision";

    @PostConstruct
    public void startConsumer() {
        log.info("🚀 Iniciando consumer resiliente para topic: {}", TOPIC_NAME);

        // TODO: Descomentar cuando se resuelvan los tipos
        // consumeGuiaRemisionEvents()
        // .doOnError(error -> log.error("❌ Error fatal en consumer: {}",
        // error.getMessage(), error))
        // .doOnComplete(() -> log.info("✅ Consumer completado"))
        // .subscribe();
    }

    public Flux<Void> consumeGuiaRemisionEvents() {
        // ✅ Configuración de ReceiverOptions con backpressure
        ReceiverOptions<String, Object> options = receiverOptions
                .subscription(Collections.singletonList(TOPIC_NAME))
                .consumerProperty("max.poll.records",
                        properties.getKafka().getConsumer().getBackpressure().getPrefetch());

        return KafkaReceiver.create(options)
                .receive()
                // ✅ BACKPRESSURE BUFFER - Evita OOM en picos de tráfico
                .onBackpressureBuffer(
                        properties.getKafka().getConsumer().getBackpressure().getBufferSize(),
                        error -> log.error("🚫 Buffer de backpressure lleno: {}", error.toString()))
                // ✅ RATE LIMITING - Controla la velocidad de procesamiento
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                // ✅ CIRCUIT BREAKER - Protege contra fallas en cascada
                .transformDeferred(CircuitBreakerOperator.of(consumerCircuitBreaker))
                // ✅ BULKHEAD - Aísla el procesamiento de eventos
                .transformDeferred(BulkheadOperator.of(processingBulkhead))
                // ✅ PARALELISMO CONTROLADO - Procesa en paralelo pero limitado
                .parallel(properties.getKafka().getConsumer().getProcessing().getParallelism())
                .runOn(Schedulers.boundedElastic())
                // TODO: Descomentar cuando se implemente processRecord
                // .flatMap(this::processRecord)
                .map(record -> {
                    // Procesamiento simple temporal
                    log.debug("📨 Record recibido: {}", record.topic());
                    record.receiverOffset().acknowledge();
                    return (Void) null;
                })
                .sequential()
                // ✅ RETRY STRATEGY - Reintentos con backoff exponencial
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(10))
                        .jitter(0.1)
                        .doBeforeRetry(signal -> log.warn("🔄 Reintentando procesamiento tras error: {}",
                                signal.failure().getMessage())))
                // ✅ ERROR HANDLING - Manejo graceful de errores no recuperables
                .onErrorResume(error -> {
                    log.error("💥 Error no recuperable en consumer: {}", error.getMessage(), error);
                    // En un caso real, podrías enviar a DLQ o notificar alertas
                    return Mono.empty();
                });
    }

    /*
     * // TODO: Revisar tipos de Reactor - método temporalmente comentado
     * private Mono<Void> processRecord(ReceiverRecord<String, Object> record) {
     * String correlationId = extractCorrelationId(record);
     * 
     * log.
     * info("📨 Procesando evento - Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}"
     * ,
     * record.topic(),
     * record.partition(),
     * record.offset(),
     * correlationId);
     * 
     * // TODO: Implementar lógica de procesamiento completa
     * return Mono.fromRunnable(() -> {
     * // ✅ Deserialización y procesamiento simplificado
     * if (record.value() instanceof GuiaRemisionRemitenteMessage message) {
     * log.info("🔄 Procesando guía de remisión - ID: {}, Cliente: {}",
     * message.getIdComprobante(), message.getIdCliente());
     * // Aquí va tu lógica de negocio
     * }
     * 
     * // ✅ Confirmar offset
     * record.receiverOffset().acknowledge();
     * log.debug("✅ Offset confirmado - Partition: {}, Offset: {}",
     * record.partition(), record.offset());
     * })
     * .onErrorResume(error -> {
     * log.error("❌ Error procesando record: {}", error.getMessage());
     * record.receiverOffset().acknowledge();
     * return Mono.empty();
     * });
     * }
     */

    private Mono<Void> processGuiaRemisionMessage(GuiaRemisionRemitenteMessage message) {
        return Mono.<Void>fromRunnable(() -> {
            log.info("🔄 Procesando guía de remisión - ID Comprobante: {}, Cliente: {}, Items: {}",
                    message.getIdComprobante(),
                    message.getIdCliente(),
                    message.getDetailItems().size());

            // ✅ AQUÍ VA TU LÓGICA DE NEGOCIO
            // Ejemplo:
            // 1. Validar mensaje
            // 2. Generar comprobante SUNAT
            // 3. Guardar en base de datos
            // 4. Enviar respuesta por Kafka

            // Simular procesamiento
            try {
                Thread.sleep(100); // Simular trabajo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Procesamiento interrumpido", e);
            }

            log.info("✅ Guía de remisión procesada exitosamente - ID: {}", message.getIdComprobante());
        })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String extractCorrelationId(ReceiverRecord<String, Object> record) {
        // Extraer correlation ID de headers si existe
        return record.headers().lastHeader("correlationId") != null
                ? new String(record.headers().lastHeader("correlationId").value())
                : "unknown-" + System.currentTimeMillis();
    }
}