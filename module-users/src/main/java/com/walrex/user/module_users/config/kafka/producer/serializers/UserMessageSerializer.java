package com.walrex.user.module_users.config.kafka.producer.serializers;

import com.walrex.avro.schemas.RoleMessage;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class UserMessageSerializer implements Serializer<RoleMessage> {
    private final KafkaAvroSerializer kafkaAvroSerializer;

    public UserMessageSerializer() {
        this.kafkaAvroSerializer = new KafkaAvroSerializer();
    }

    public UserMessageSerializer(SchemaRegistryClient schemaRegistryClient) {
        this.kafkaAvroSerializer = new KafkaAvroSerializer(schemaRegistryClient);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        kafkaAvroSerializer.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, RoleMessage data) {
        return kafkaAvroSerializer.serialize(topic, data);
    }

    @Override
    public void close() {
        kafkaAvroSerializer.close();
    }
}
