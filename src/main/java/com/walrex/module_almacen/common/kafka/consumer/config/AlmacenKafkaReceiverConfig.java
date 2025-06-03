package com.walrex.module_almacen.common.kafka.consumer.config;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AlmacenKafkaReceiverConfig {
    private final SchemaRegistryClient schemaRegistryClient;
    private final Map<String, Object> schemaRegistryConfig;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String offset_reset;

    // Constructor con Qualifiers específicos
    public AlmacenKafkaReceiverConfig(
            @Qualifier("AlmacenSchemaRegistryClient") SchemaRegistryClient schemaRegistryClient,
            @Qualifier("AlmacenSchemaRegistryConfig") Map<String, Object> schemaRegistryConfig
    ) {
        this.schemaRegistryClient = schemaRegistryClient;
        this.schemaRegistryConfig = schemaRegistryConfig;
    }

    @Bean(name = "almacenModuleConsumerProps")
    public Map<String, Object> almacenModuleConsumerProps(){
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

    @Bean(name = "almacenModuleReceiveOptions")
    public ReceiverOptions<String, Object> almacenModuleReceiveOptions() {
        return ReceiverOptions.create(almacenModuleConsumerProps());
    }
}