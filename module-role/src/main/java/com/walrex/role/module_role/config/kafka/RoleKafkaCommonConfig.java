package com.walrex.role.module_role.config.kafka;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class RoleKafkaCommonConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean(name = "roleModuleSchemaRegistryClient")
    public SchemaRegistryClient roleModuleSchemaRegistryClient() {
        return new CachedSchemaRegistryClient(schemaRegistryUrl, 100);
    }

    @Bean(name = "roleModuleSchemaRegistryConfig")
    public Map<String, Object> roleModuleSchemaRegistryConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        //config.put(KafkaAvroDeserializerConfig.USE_LATEST_VERSION, true);
        //config.put(KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS, true);
        config.put(KafkaAvroDeserializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicNameStrategy.class.getName());
        return config;
    }
}
