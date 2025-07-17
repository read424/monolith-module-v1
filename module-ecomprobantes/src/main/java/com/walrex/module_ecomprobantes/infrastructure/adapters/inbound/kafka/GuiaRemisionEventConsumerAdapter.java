package com.walrex.module_ecomprobantes.infrastructure.adapters.inbound.kafka;

import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.module_ecomprobantes.application.ports.input.ProcesarGuiaRemisionUseCase;
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
        private final ProcesarGuiaRemisionUseCase procesarGuiaRemisionUseCase;
        private final String TOPIC_NAME;

        public GuiaRemisionEventConsumerAdapter(
                        ComprobantesKafkaProperties properties,
                        @Qualifier("comprobanteModuleReceiveOptions") ReceiverOptions<String, Object> receiverOptions,
                        @Qualifier("comprobantesKafkaConsumerCircuitBreaker") CircuitBreaker consumerCircuitBreaker,
                        @Qualifier("comprobantesKafkaEventsRateLimiter") RateLimiter rateLimiter,
                        @Qualifier("comprobantesKafkaProcessingBulkhead") Bulkhead processingBulkhead,
                        ProcesarGuiaRemisionUseCase procesarGuiaRemisionUseCase,
                        @Value("${kafka.topics.almacen.create-comprobante-guia-remision}") String topicName) {
                this.properties = properties;
                this.receiverOptions = receiverOptions;
                this.consumerCircuitBreaker = consumerCircuitBreaker;
                this.rateLimiter = rateLimiter;
                this.processingBulkhead = processingBulkhead;
                this.procesarGuiaRemisionUseCase = procesarGuiaRemisionUseCase;
                this.TOPIC_NAME = topicName;
        }

        @PostConstruct
        public void startConsumer() {
                log.info("🚀 Iniciando consumer resiliente para topic: {}", TOPIC_NAME);

                consumeGuiaRemisionEvents()
                                .doOnError(error -> log.error("❌ Error fatal en consumer: {}", error.getMessage(),
                                                error))
                                .doOnComplete(() -> log.info("✅ Consumer completado"))
                                .subscribe();
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
                                                error -> log.error("🚫 Buffer de backpressure lleno: {}",
                                                                error.toString()))
                                // ✅ RATE LIMITING - Controla la velocidad de procesamiento
                                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                                // ✅ CIRCUIT BREAKER - Protege contra fallas en cascada
                                .transformDeferred(CircuitBreakerOperator.of(consumerCircuitBreaker))
                                // ✅ BULKHEAD - Aísla el procesamiento de eventos
                                .transformDeferred(BulkheadOperator.of(processingBulkhead))
                                // ✅ PARALELISMO CONTROLADO - Procesa en paralelo pero limitado
                                .parallel(properties.getKafka().getConsumer().getProcessing().getParallelism())
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
                        if (record.value() instanceof CreateGuiaRemisionRemitenteMessage message) {
                                log.info("🔄 Procesando guía de remisión - Guia: {}, Items: {}, CorrelationId: {}",
                                                message, message.getDetailItems().size(), correlationId);

                                // ✅ Procesar con el use case y confirmar offset al finalizar
                                return procesarGuiaRemisionUseCase.procesarGuiaRemision(message, correlationId)
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