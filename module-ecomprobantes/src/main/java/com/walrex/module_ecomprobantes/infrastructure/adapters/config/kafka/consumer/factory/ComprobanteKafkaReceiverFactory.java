package com.walrex.module_ecomprobantes.infrastructure.adapters.config.kafka.consumer.factory;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component("ComprobanteKafkaReceiverFactory")
public class ComprobanteKafkaReceiverFactory {
    private final ReceiverOptions<String, Object> kafkaReceiverOptions;

    public ComprobanteKafkaReceiverFactory(@Qualifier("comprobanteModuleReceiveOptions") ReceiverOptions<String, Object> kafkaReceiverOptions) {
        this.kafkaReceiverOptions = kafkaReceiverOptions;
    }

    public <T> KafkaReceiver<String, T> createReceiver(String topicName){
        Map<String, Object> props = new HashMap<>(kafkaReceiverOptions.consumerProperties());
        ReceiverOptions<String, T> options =
            ReceiverOptions.<String, T>create(props)
                .subscription(Collections.singletonList(topicName));
        return KafkaReceiver.create(options);
    }

    /**
     * Verifica si un topic existe en Kafka
     * @param topicName Nombre del topic a verificar
     * @param properties Propiedades del consumidor de Kafka
     * @return true si el topic existe, false en caso contrario
     */
    private static boolean doesTopicExist(String topicName, Map<String, Object> properties) {
        try (var admin = AdminClient.create(properties)) {
            return admin.listTopics().names().get().contains(topicName);
        } catch (Exception e) {
            return false;
        }
    }
}
