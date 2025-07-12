package com.walrex.module_ecomprobantes.infrastructure.adapters.config.kafka.producer.serializers;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.walrex.avro.schemas.GuiaRemisionRemitenteResponse;

import io.confluent.kafka.serializers.KafkaAvroSerializer;

public class GRRResponseMessageSerializer implements Serializer<GuiaRemisionRemitenteResponse> {
    private final KafkaAvroSerializer kafkaAvroSerializer;

    public GRRResponseMessageSerializer() {
        this.kafkaAvroSerializer = new KafkaAvroSerializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        kafkaAvroSerializer.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, GuiaRemisionRemitenteResponse data) {
        return kafkaAvroSerializer.serialize(topic, data);
    }

    @Override
    public void close() {
        kafkaAvroSerializer.close();
    }
}
