package com.walrex.module_mailing.config.kafka.consumer.factory;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component("mailingModuleKafkaConsumerFactory")
public class MailingKafkaConsumerFactory {
    private final ReceiverOptions<String, Object> kafkaReceiverOptions;

    // Constructor con Qualifier específico
    public MailingKafkaConsumerFactory(@Qualifier("mailingModuleReceiverOptions") ReceiverOptions<String, Object> kafkaReceiverOptions) {
        this.kafkaReceiverOptions = kafkaReceiverOptions;
    }

    public <T> KafkaReceiver<String, T> createReceiver(String topicName){
        Map<String, Object> props = new HashMap<>(kafkaReceiverOptions.consumerProperties());
        // Si la validación es necesaria, puede hacerse solo en entornos específicos
        if (!doesTopicExist(topicName, props)) {
            throw new IllegalArgumentException("Topic does not exist: " + topicName);
        }
        ReceiverOptions<String, T> options =
                ReceiverOptions.<String, T>create(props)
                        .subscription(Collections.singletonList(topicName));
        return KafkaReceiver.create(options);
    }

    private static boolean doesTopicExist(String topicName, Map<String, Object> properties) {
        try (var admin = AdminClient.create(properties)) {
            return admin.listTopics().names().get().contains(topicName);
        } catch (Exception e) {
            return false;
        }
    }
}
