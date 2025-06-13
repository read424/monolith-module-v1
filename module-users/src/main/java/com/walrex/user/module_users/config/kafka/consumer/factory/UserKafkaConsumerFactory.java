package com.walrex.user.module_users.config.kafka.consumer.factory;

import com.walrex.avro.schemas.RoleResponseMessage;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverPartition;

import java.util.*;

@Component("userModuleKafkaConsumerFactory")
public class UserKafkaConsumerFactory {
    private final ReceiverOptions<String, Object> kafkaReceiverOptions;

    // Constructor con @Qualifier
    public UserKafkaConsumerFactory(@Qualifier("userModuleReceiverOptions") ReceiverOptions<String, Object> kafkaReceiverOptions) {
        this.kafkaReceiverOptions = kafkaReceiverOptions;
    }

    public <T> KafkaReceiver<String, T> createReceiver(String topicName){
        Map<String, Object> props = new HashMap<>(kafkaReceiverOptions.consumerProperties());
        // Si la validación es necesaria, puede hacerse solo en entornos específicos
        //if (!doesTopicExist(topicName, props)) {
        //    throw new IllegalArgumentException("Topic does not exist: " + topicName);
        //}
        ReceiverOptions<String, T> options =
                ReceiverOptions.<String, T>create(props)
                        .subscription(Collections.singletonList(topicName));
        return KafkaReceiver.create(options);
    }

    public KafkaReceiver<String, Object> createKafkaReceiver(String topicName){
        var kafkaOptions = getKafkaOptions(topicName);
        return KafkaReceiver.<String, Object>create(kafkaOptions);
    }

    public ReceiverOptions<String, Object> getKafkaOptions(String topicName){
        var consumerProperties = kafkaReceiverOptions.consumerProperties();
        //if (!doesTopicExist(topicName, consumerProperties)) {
        //    throw new IllegalArgumentException("Topic does not exist: " + topicName);
        //}

        var kafkaOptions = kafkaReceiverOptions.subscription(List.of(topicName));
        kafkaOptions.addAssignListener(partitions ->
                partitions.forEach(ReceiverPartition::seekToBeginning));
        return kafkaOptions;
    }

    /**
     * Crea un KafkaReceiver específicamente tipado para recibir RoleResponseMessage
     * @param topicName Nombre del topic a escuchar
     * @return KafkaReceiver configurado para RoleResponseMessage
     */
    public KafkaReceiver<String, RoleResponseMessage> receiverRoleResponseMessage(String topicName) {
        Map<String, Object> props = new HashMap<>(kafkaReceiverOptions.consumerProperties());
        //if (!doesTopicExist(topicName, props)) {
        //    throw new IllegalArgumentException("Topic does not exist: " + topicName);
        //}

        ReceiverOptions<String, RoleResponseMessage> options =
                ReceiverOptions.<String, RoleResponseMessage>create(props)
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
