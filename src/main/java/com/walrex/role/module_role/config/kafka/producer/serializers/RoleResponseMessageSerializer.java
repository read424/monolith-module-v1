package com.walrex.role.module_role.config.kafka.producer.serializers;

import com.walrex.avro.schemas.RoleResponseMessage;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class RoleResponseMessageSerializer implements Serializer<RoleResponseMessage> {
    private final KafkaAvroSerializer kafkaAvroSerializer;

    public RoleResponseMessageSerializer() { this.kafkaAvroSerializer = new KafkaAvroSerializer(); }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey){ kafkaAvroSerializer.configure(configs, isKey); }

    @Override
    public byte[] serialize(String topic, RoleResponseMessage data){ return kafkaAvroSerializer.serialize(topic, data); }

    @Override
    public void close() { kafkaAvroSerializer.close();}

}
