package com.walrex.kafka.aop.aspects;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.walrex.kafka.aop.annotations.KafkaConsumer;
import com.walrex.kafka.aop.factory.ReactiveKafkaConsumerFactory;
import com.walrex.kafka.aop.manager.KafkaMetricsManager;
import com.walrex.kafka.aop.serialization.AvroSerializationHelper;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

/**
 * 🎭 Aspecto AOP para automatizar Kafka Consumer
 * 
 * Funcionalidades automáticas:
 * - Configuración de ReceiverOptions
 * - Deserialización Avro
 * - Patterns de resilience
 * - Backpressure y buffering
 * - Métricas y logging
 * - Error handling
 * 
 * @author Kafka AOP Module
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerAspect {

    private final ReactiveKafkaConsumerFactory consumerFactory;
    private final AvroSerializationHelper avroHelper;
    private final KafkaMetricsManager metricsManager;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * 🎯 Intercepta métodos anotados con @KafkaConsumer
     */
    @Around("@annotation(kafkaConsumer)")
    public Object handleKafkaConsumer(ProceedingJoinPoint joinPoint, KafkaConsumer kafkaConsumer) {
        String consumerName = generateConsumerName(joinPoint, kafkaConsumer);
        
        log.info("🚀 Iniciando consumer AOP: {} para topic: {}", consumerName, kafkaConsumer.topic());

        try {
            // ✅ 1. Crear ReceiverOptions automáticamente
            ReceiverOptions<String, Object> receiverOptions = createReceiverOptions(kafkaConsumer);

            // ✅ 2. Crear componentes de resilience
            var resilience = createResilienceComponents(kafkaConsumer, consumerName);

            // ✅ 3. Crear y iniciar consumer reactivo
            return createReactiveConsumer(joinPoint, kafkaConsumer, receiverOptions, resilience, consumerName);

        } catch (Exception e) {
            log.error("❌ Error configurando consumer AOP: {}", e.getMessage(), e);
            return Mono.error(e);
        }
    }

    /**
     * 🔧 Crea ReceiverOptions con configuración automática
     */
    private ReceiverOptions<String, Object> createReceiverOptions(KafkaConsumer kafkaConsumer) {
        Map<String, Object> consumerProps = new HashMap<>();
        
        // ✅ Configuración básica
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumer.groupId());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumer.autoOffsetReset());
        
        // ✅ Deserializers
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        
        // ✅ Configuración Avro automática
        consumerProps.putAll(avroHelper.getSchemaRegistryConfig(kafkaConsumer.schemaRegistryUrl()));
        consumerProps.put("specific.avro.reader", kafkaConsumer.specificAvroReader());
        
        // ✅ Configuración de backpressure
        var backpressure = kafkaConsumer.backpressure();
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, backpressure.prefetch());
        consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        consumerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        // ✅ Configuración personalizada
        applyCustomProperties(consumerProps, kafkaConsumer.customProperties());
        
        return ReceiverOptions.<String, Object>create(consumerProps)
                .subscription(Collections.singletonList(kafkaConsumer.topic()));
    }

    /**
     * 🛡️ Crea componentes de resilience
     */
    private ResilienceComponents createResilienceComponents(KafkaConsumer kafkaConsumer, String consumerName) {
        var resilience = kafkaConsumer.resilience();
        
        CircuitBreaker circuitBreaker = null;
        RateLimiter rateLimiter = null;
        Bulkhead bulkhead = null;
        
        if (resilience.enableCircuitBreaker()) {
            String cbName = resilience.circuitBreaker().isEmpty() 
                ? consumerName + "-circuit-breaker" 
                : resilience.circuitBreaker();
            circuitBreaker = consumerFactory.createCircuitBreaker(cbName);
        }
        
        if (resilience.enableRateLimiter()) {
            rateLimiter = consumerFactory.createRateLimiter(consumerName + "-rate-limiter", resilience.rateLimit());
        }
        
        if (resilience.enableBulkhead()) {
            bulkhead = consumerFactory.createBulkhead(consumerName + "-bulkhead", resilience.bulkhead());
        }
        
        return new ResilienceComponents(circuitBreaker, rateLimiter, bulkhead);
    }

    /**
     * 🔄 Crea el consumer reactivo completo
     */
    private Flux<Void> createReactiveConsumer(
            ProceedingJoinPoint joinPoint, 
            KafkaConsumer kafkaConsumer, 
            ReceiverOptions<String, Object> receiverOptions,
            ResilienceComponents resilience,
            String consumerName) {

        return KafkaReceiver.create(receiverOptions)
                .receive()
                // ✅ Backpressure buffer
                .onBackpressureBuffer(
                    kafkaConsumer.backpressure().bufferSize(),
                    error -> log.error("🚫 Buffer overflow en consumer {}: {}", consumerName, error.toString())
                )
                // ✅ Rate limiting
                .transform(flux -> resilience.rateLimiter != null 
                    ? flux.transformDeferred(io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator.of(resilience.rateLimiter))
                    : flux)
                // ✅ Circuit breaker
                .transform(flux -> resilience.circuitBreaker != null
                    ? flux.transformDeferred(io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator.of(resilience.circuitBreaker))
                    : flux)
                // ✅ Bulkhead
                .transform(flux -> resilience.bulkhead != null
                    ? flux.transformDeferred(io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator.of(resilience.bulkhead))
                    : flux)
                // ✅ Paralelismo controlado
                .parallel(kafkaConsumer.backpressure().parallelism())
                .runOn(Schedulers.boundedElastic())
                // ✅ Procesamiento del mensaje
                .flatMap(record -> processRecord(joinPoint, kafkaConsumer, record, consumerName))
                .sequential()
                // ✅ Retry strategy
                .retryWhen(Retry.backoff(
                    kafkaConsumer.resilience().retry().maxAttempts(),
                    Duration.ofMillis(kafkaConsumer.resilience().retry().backoffDelay()))
                    .maxBackoff(Duration.ofMillis(kafkaConsumer.resilience().retry().maxBackoffDelay()))
                    .jitter(kafkaConsumer.resilience().retry().jitter())
                    .doBeforeRetry(signal -> log.warn("🔄 Reintentando consumer {}: {}", 
                        consumerName, signal.failure().getMessage()))
                )
                // ✅ Error handling global
                .onErrorResume(error -> {
                    log.error("💥 Error fatal en consumer {}: {}", consumerName, error.getMessage(), error);
                    // TODO: Enviar a DLQ si está configurado
                    return Mono.empty();
                })
                // ✅ Métricas automáticas
                .doOnNext(v -> {
                    if (kafkaConsumer.enableMetrics()) {
                        metricsManager.recordConsumerMessage(consumerName, kafkaConsumer.topic());
                    }
                });
    }

    /**
     * 📨 Procesa un record individual
     */
    private Mono<Void> processRecord(
            ProceedingJoinPoint joinPoint,
            KafkaConsumer kafkaConsumer, 
            ReceiverRecord<String, Object> record,
            String consumerName) {

        String correlationId = extractCorrelationId(record);
        
        if (kafkaConsumer.enableLogging()) {
            log.info("📨 Procesando mensaje - Consumer: {}, Topic: {}, Partition: {}, Offset: {}, CorrelationId: {}",
                consumerName, record.topic(), record.partition(), record.offset(), correlationId);
        }

        return Mono.fromCallable(() -> {
            try {
                // ✅ Deserialización automática del valor
                Object deserializedValue = avroHelper.deserialize(record.value(), kafkaConsumer.schemaClass());
                
                // ✅ Invocar método original con valor deserializado
                Object result = joinPoint.proceed(new Object[]{deserializedValue});
                
                // ✅ Confirmar offset
                record.receiverOffset().acknowledge();
                
                if (kafkaConsumer.enableLogging()) {
                    log.debug("✅ Mensaje procesado exitosamente - CorrelationId: {}", correlationId);
                }
                
                return result;
            } catch (Throwable e) {
                log.error("❌ Error procesando mensaje - CorrelationId: {}, Error: {}", correlationId, e.getMessage());
                throw new RuntimeException("Error procesando mensaje Kafka", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofMillis(kafkaConsumer.processingTimeoutMs()))
        .then();
    }

    /**
     * 🔍 Extrae correlation ID de headers
     */
    private String extractCorrelationId(ReceiverRecord<String, Object> record) {
        var correlationHeader = record.headers().lastHeader("correlationId");
        return correlationHeader != null 
            ? new String(correlationHeader.value()) 
            : "unknown-" + UUID.randomUUID();
    }

    /**
     * 📝 Genera nombre único para el consumer
     */
    private String generateConsumerName(ProceedingJoinPoint joinPoint, KafkaConsumer kafkaConsumer) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName + "." + kafkaConsumer.topic();
    }

    /**
     * ⚙️ Aplica propiedades personalizadas
     */
    private void applyCustomProperties(Map<String, Object> props, String[] customProperties) {
        for (String prop : customProperties) {
            String[] parts = prop.split("=", 2);
            if (parts.length == 2) {
                props.put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    /**
     * 🛡️ Record para componentes de resilience
     */
    private record ResilienceComponents(
        CircuitBreaker circuitBreaker,
        RateLimiter rateLimiter,
        Bulkhead bulkhead
    ) {}
} 