package com.walrex.module_almacen.common.Metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KafkaMetrics implements MeterBinder {
    private final MeterRegistry registry;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final Logger log = LoggerFactory.getLogger(KafkaMetrics.class);
    private List<KafkaClientMetrics> clientMetrics = new ArrayList<>();

    public KafkaMetrics(String bootstrapServers, MeterRegistry registry) {
        this.bootstrapServers = bootstrapServers;
        this.registry = registry;
        // Programar recolección periódica de métricas de lag
        startLagMetricsCollection();
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        // Crear un consumidor y productor temporales para obtener métricas
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", "metrics-consumer-group");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset", "latest");

        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put("bootstrap.servers", bootstrapServers);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            Producer<String, String> producer = new KafkaProducer<>(producerProps);
            Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps);

            // Registrar métricas de clientes Kafka
            KafkaClientMetrics producerMetrics = new KafkaClientMetrics(producer);
            KafkaClientMetrics consumerMetrics = new KafkaClientMetrics(consumer);

            producerMetrics.bindTo(registry);
            consumerMetrics.bindTo(registry);

            clientMetrics.add(producerMetrics);
            clientMetrics.add(consumerMetrics);

            // Registrar un shutdown hook para limpiar recursos
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                producer.close();
                consumer.close();
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));

        } catch (Exception e) {
            log.error("Error al inicializar métricas de Kafka", e);
        }
    }

    private void startLagMetricsCollection() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Recopilar métricas de lag para todos los grupos de consumidores
                collectLagMetrics();
            } catch (Exception e) {
                log.error("Error al recopilar métricas de lag", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void collectLagMetrics() {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put("bootstrap.servers", bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            // Obtener todos los grupos de consumidores
            adminClient.listConsumerGroups().all().get().forEach(group -> {
                String groupId = group.groupId();

                // Solo considerar grupos activos
                if (!groupId.startsWith("metrics-consumer")) {
                    try {
                        // Obtener información de offset para este grupo
                        adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get()
                                .forEach((topicPartition, offsetAndMetadata) -> {
                                    try {
                                        // Obtener el end offset (último mensaje disponible)
                                        Map<TopicPartition, Long> endOffsets = adminClient.listOffsets(
                                                        Collections.singletonMap(topicPartition, OffsetSpec.latest()))
                                                .all().get().entrySet().stream()
                                                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().offset()), HashMap::putAll);

                                        long consumerOffset = offsetAndMetadata.offset();
                                        long endOffset = endOffsets.getOrDefault(topicPartition, 0L);
                                        long lag = Math.max(0, endOffset - consumerOffset);

                                        // Registrar métrica de lag
                                        registry.gauge("kafka.consumer.lag",
                                                Tags.of(
                                                        Tag.of("group", groupId),
                                                        Tag.of("topic", topicPartition.topic()),
                                                        Tag.of("partition", String.valueOf(topicPartition.partition()))
                                                ),
                                                lag);

                                        if (lag > 0) {
                                            log.debug("Consumer lag: group={}, topic={}, partition={}, lag={}",
                                                    groupId, topicPartition.topic(), topicPartition.partition(), lag);
                                        }
                                    } catch (Exception e) {
                                        log.error("Error al obtener end offset para {}", topicPartition, e);
                                    }
                                });
                    } catch (Exception e) {
                        log.error("Error al obtener offsets para grupo {}", groupId, e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Error al obtener grupos de consumidores", e);
        }
    }
}
