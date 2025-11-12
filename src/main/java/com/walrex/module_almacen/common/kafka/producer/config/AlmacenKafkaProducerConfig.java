package com.walrex.module_almacen.common.kafka.producer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class AlmacenKafkaProducerConfig {

    private Map<String, Object> schemaRegistryConfig;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Inyección por constructor con Qualifier específico
    public AlmacenKafkaProducerConfig(
            @Qualifier("AlmacenSchemaRegistryConfig") Map<String, Object> schemaRegistryConfig) {
        this.schemaRegistryConfig = schemaRegistryConfig;
    }

    @Bean(name = "almacenModuleProducerConfigs")
    public Map<String, Object> almacenModuleProducerConfigs() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        // Configuraciones adicionales para mejorar estabilidad
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024); // 32KB

        // Límites de tiempo para detectar problemas rápidamente
        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        producerProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);

        // Incluir configuración del Schema Registry
        producerProps.putAll(schemaRegistryConfig);

        // Log para debug
        System.out.println("🔧 Producer Properties: " + producerProps);
        System.out.println("🔧 Schema Registry URL: " + producerProps.get("schema.registry.url"));

        return producerProps;
    }

    @Bean(name = "almacenModuleProducerOptions")
    public SenderOptions<String, Object> almacenModuleProducerOptions() {
        return SenderOptions.create(almacenModuleProducerConfigs());
    }
}
