package com.walrex.kafka.aop.manager;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 📊 Manager para métricas automáticas de Kafka AOP
 * 
 * Funcionalidades:
 * - Contadores de mensajes por consumer/producer
 * - Latencias de procesamiento
 * - Tasas de error y éxito
 * - Throughput por topic
 * - Métricas de resilience
 * - Exportación a sistemas de monitoreo
 * 
 * @author Kafka AOP Module
 */
@Component
@Slf4j
public class KafkaMetricsManager {

    private final Map<String, ConsumerMetrics> consumerMetrics = new ConcurrentHashMap<>();
    private final Map<String, ProducerMetrics> producerMetrics = new ConcurrentHashMap<>();
    private final Map<String, TopicMetrics> topicMetrics = new ConcurrentHashMap<>();

    /**
     * 📈 Registra mensaje procesado por consumer
     */
    public void recordConsumerMessage(String consumerName, String topic) {
        // ✅ Métricas del consumer
        consumerMetrics.computeIfAbsent(consumerName, k -> new ConsumerMetrics(k))
            .recordMessage();

        // ✅ Métricas del topic
        topicMetrics.computeIfAbsent(topic, k -> new TopicMetrics(k))
            .recordConsumedMessage();

        log.debug("📊 Mensaje registrado - Consumer: {}, Topic: {}", consumerName, topic);
    }

    /**
     * 📤 Registra mensaje enviado por producer
     */
    public void recordProducerMessage(String producerName, String topic, boolean success) {
        // ✅ Métricas del producer
        producerMetrics.computeIfAbsent(producerName, k -> new ProducerMetrics(k))
            .recordMessage(success);

        // ✅ Métricas del topic
        topicMetrics.computeIfAbsent(topic, k -> new TopicMetrics(k))
            .recordProducedMessage(success);

        log.debug("📊 Mensaje enviado registrado - Producer: {}, Topic: {}, Success: {}", 
            producerName, topic, success);
    }

    /**
     * ⏱️ Registra latencia de procesamiento
     */
    public void recordProcessingLatency(String consumerName, Duration latency) {
        consumerMetrics.computeIfAbsent(consumerName, k -> new ConsumerMetrics(k))
            .recordLatency(latency);

        log.debug("⏱️ Latencia registrada - Consumer: {}, Latency: {}ms", 
            consumerName, latency.toMillis());
    }

    /**
     * 🔄 Registra retry de consumer
     */
    public void recordConsumerRetry(String consumerName, String topic, int attemptNumber) {
        consumerMetrics.computeIfAbsent(consumerName, k -> new ConsumerMetrics(k))
            .recordRetry(attemptNumber);

        log.debug("🔄 Retry registrado - Consumer: {}, Topic: {}, Attempt: {}", 
            consumerName, topic, attemptNumber);
    }

    /**
     * ❌ Registra error de consumer
     */
    public void recordConsumerError(String consumerName, String topic, Throwable error) {
        consumerMetrics.computeIfAbsent(consumerName, k -> new ConsumerMetrics(k))
            .recordError(error);

        topicMetrics.computeIfAbsent(topic, k -> new TopicMetrics(k))
            .recordError();

        log.debug("❌ Error registrado - Consumer: {}, Topic: {}, Error: {}", 
            consumerName, topic, error.getClass().getSimpleName());
    }

    /**
     * 🔵 Registra evento de circuit breaker
     */
    public void recordCircuitBreakerEvent(String circuitBreakerName, String event) {
        // TODO: Implementar métricas específicas de circuit breaker
        log.debug("🔵 Circuit Breaker Event - Name: {}, Event: {}", circuitBreakerName, event);
    }

    /**
     * 📊 Obtiene métricas de consumer
     */
    public ConsumerMetrics getConsumerMetrics(String consumerName) {
        return consumerMetrics.get(consumerName);
    }

    /**
     * 📤 Obtiene métricas de producer
     */
    public ProducerMetrics getProducerMetrics(String producerName) {
        return producerMetrics.get(producerName);
    }

    /**
     * 📋 Obtiene métricas de topic
     */
    public TopicMetrics getTopicMetrics(String topic) {
        return topicMetrics.get(topic);
    }

    /**
     * 📈 Obtiene resumen de todas las métricas
     */
    public MetricsSummary getMetricsSummary() {
        long totalConsumerMessages = consumerMetrics.values().stream()
            .mapToLong(m -> m.totalMessages.get()).sum();
        
        long totalProducerMessages = producerMetrics.values().stream()
            .mapToLong(m -> m.successfulMessages.get() + m.failedMessages.get()).sum();
        
        long totalErrors = consumerMetrics.values().stream()
            .mapToLong(m -> m.errorCount.get()).sum() +
            producerMetrics.values().stream()
            .mapToLong(m -> m.failedMessages.get()).sum();

        return new MetricsSummary(
            consumerMetrics.size(),
            producerMetrics.size(),
            topicMetrics.size(),
            totalConsumerMessages,
            totalProducerMessages,
            totalErrors
        );
    }

    /**
     * 🧹 Limpia métricas antiguas
     */
    public void cleanupOldMetrics(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        
        consumerMetrics.entrySet().removeIf(entry -> entry.getValue().createdAt.isBefore(cutoff));
        producerMetrics.entrySet().removeIf(entry -> entry.getValue().createdAt.isBefore(cutoff));
        topicMetrics.entrySet().removeIf(entry -> entry.getValue().createdAt.isBefore(cutoff));
        
        log.info("🧹 Métricas antiguas limpiadas (older than {})", maxAge);
    }

    /**
     * 📊 Métricas de Consumer
     */
    public static class ConsumerMetrics {
        public final String name;
        public final Instant createdAt;
        public final AtomicLong totalMessages = new AtomicLong();
        public final AtomicLong errorCount = new AtomicLong();
        public final AtomicLong retryCount = new AtomicLong();
        public final AtomicLong totalLatencyMs = new AtomicLong();
        public final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();

        public ConsumerMetrics(String name) {
            this.name = name;
            this.createdAt = Instant.now();
        }

        public void recordMessage() {
            totalMessages.incrementAndGet();
        }

        public void recordError(Throwable error) {
            errorCount.incrementAndGet();
            errorsByType.computeIfAbsent(error.getClass().getSimpleName(), k -> new AtomicLong())
                .incrementAndGet();
        }

        public void recordRetry(int attemptNumber) {
            retryCount.incrementAndGet();
        }

        public void recordLatency(Duration latency) {
            totalLatencyMs.addAndGet(latency.toMillis());
        }

        public double getAverageLatencyMs() {
            long total = totalMessages.get();
            return total > 0 ? (double) totalLatencyMs.get() / total : 0.0;
        }

        public double getErrorRate() {
            long total = totalMessages.get();
            return total > 0 ? (double) errorCount.get() / total : 0.0;
        }
    }

    /**
     * 📤 Métricas de Producer
     */
    public static class ProducerMetrics {
        public final String name;
        public final Instant createdAt;
        public final AtomicLong successfulMessages = new AtomicLong();
        public final AtomicLong failedMessages = new AtomicLong();
        public final AtomicLong totalLatencyMs = new AtomicLong();

        public ProducerMetrics(String name) {
            this.name = name;
            this.createdAt = Instant.now();
        }

        public void recordMessage(boolean success) {
            if (success) {
                successfulMessages.incrementAndGet();
            } else {
                failedMessages.incrementAndGet();
            }
        }

        public long getTotalMessages() {
            return successfulMessages.get() + failedMessages.get();
        }

        public double getSuccessRate() {
            long total = getTotalMessages();
            return total > 0 ? (double) successfulMessages.get() / total : 0.0;
        }
    }

    /**
     * 📋 Métricas de Topic
     */
    public static class TopicMetrics {
        public final String name;
        public final Instant createdAt;
        public final AtomicLong consumedMessages = new AtomicLong();
        public final AtomicLong producedMessages = new AtomicLong();
        public final AtomicLong errorCount = new AtomicLong();

        public TopicMetrics(String name) {
            this.name = name;
            this.createdAt = Instant.now();
        }

        public void recordConsumedMessage() {
            consumedMessages.incrementAndGet();
        }

        public void recordProducedMessage(boolean success) {
            if (success) {
                producedMessages.incrementAndGet();
            }
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public long getTotalMessages() {
            return consumedMessages.get() + producedMessages.get();
        }
    }

    /**
     * 📈 Resumen de métricas
     */
    public record MetricsSummary(
        int totalConsumers,
        int totalProducers,
        int totalTopics,
        long totalConsumerMessages,
        long totalProducerMessages,
        long totalErrors
    ) {}
} 