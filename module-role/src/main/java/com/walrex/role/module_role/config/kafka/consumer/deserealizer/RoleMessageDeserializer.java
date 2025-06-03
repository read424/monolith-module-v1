package com.walrex.role.module_role.config.kafka.consumer.deserealizer;

import com.walrex.avro.schemas.RoleMessage;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class RoleMessageDeserializer implements Deserializer<RoleMessage> {
    private final KafkaAvroDeserializer kafkaAvroDeserializer;

    public RoleMessageDeserializer() {
        this.kafkaAvroDeserializer = new KafkaAvroDeserializer();
    }

    public RoleMessageDeserializer(SchemaRegistryClient schemaRegistryClient) {
        this.kafkaAvroDeserializer = new KafkaAvroDeserializer(schemaRegistryClient);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        kafkaAvroDeserializer.configure(configs, isKey);
    }

    @Override
    public RoleMessage deserialize(String topic, byte[] data) {
        return (RoleMessage) kafkaAvroDeserializer.deserialize(topic, data);
    }

    @Override
    public void close() {
        kafkaAvroDeserializer.close();
    }
}
