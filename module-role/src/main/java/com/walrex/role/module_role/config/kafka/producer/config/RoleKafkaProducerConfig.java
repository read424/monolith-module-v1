package com.walrex.role.module_role.config.kafka.producer.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RoleKafkaProducerConfig {

    private Map<String, Object> schemaRegistryConfig;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    // Constructor con Qualifier específico
    public RoleKafkaProducerConfig(@Qualifier("roleModuleSchemaRegistryConfig") Map<String, Object> schemaRegistryConfig) {
        this.schemaRegistryConfig = schemaRegistryConfig;
    }

    @Bean(name = "roleModuleProducerConfigs")
    public Map<String, Object> roleModuleProducerConfigs(){
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        // Incluir configuración del Schema Registry
        producerProps.putAll(schemaRegistryConfig);
        return producerProps;
    }

    @Bean(name = "roleModuleProducerOptions")
    public SenderOptions<String, Object> roleModuleProducerOptions() {
        return SenderOptions.create(roleModuleProducerConfigs());
    }
}
