package com.walrex.module_ecomprobantes.infrastructure.adapters.config.kafka.consumer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

@Configuration
public class ComprobanteKafkaConsumerConfig {
    private final Map<String, Object> schemaRegistryConfig;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String offset_reset;

    public ComprobanteKafkaConsumerConfig(
            @Qualifier("ComprobanteSchemaRegistryConfig") Map<String, Object> schemaRegistryConfig) {
        this.schemaRegistryConfig = schemaRegistryConfig;
    }

    @Bean(name = "comprobanteModuleConsumerProps")
    public Map<String, Object> comprobanteModuleConsumerProps() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset_reset);

        // Serializadores para Kafka
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());

        // Añadir toda la configuración de Schema Registry de una vez
        consumerProps.putAll(schemaRegistryConfig);

        return consumerProps;
    }

    @Bean(name = "comprobanteModuleReceiveOptions")
    public ReceiverOptions<String, Object> comprobanteModuleReceiveOptions() {
        return ReceiverOptions.<String, Object>create(comprobanteModuleConsumerProps());
    }

}
