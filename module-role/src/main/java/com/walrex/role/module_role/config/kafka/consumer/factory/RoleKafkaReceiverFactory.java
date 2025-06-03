package com.walrex.role.module_role.config.kafka.consumer.factory;

import com.walrex.avro.schemas.RoleMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverPartition;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("roleModuleKafkaConsumerFactory")
public class RoleKafkaReceiverFactory {
    private final ReceiverOptions<String, Object> kafkaReceiverOptions;

    // Constructor con Qualifier específico
    public RoleKafkaReceiverFactory(@Qualifier("roleModuleReceiverOptions") ReceiverOptions<String, Object> kafkaReceiverOptions) {
        this.kafkaReceiverOptions = kafkaReceiverOptions;
    }

    /**
     * Crea un KafkaReceiver para un topic específico que maneja objetos Avro
     * @param topicName Nombre del topic
     * @return KafkaReceiver configurado
     */
    public KafkaReceiver<String, Object> createKafkaReceiver(String topicName) {
        var kafkaOptions = getKafkaOptions(topicName);
        return KafkaReceiver.create(kafkaOptions);
    }

    /**
     * Obtiene las opciones de Kafka para un topic específico
     * @param topicName Nombre del topic
     * @return ReceiverOptions configuradas para el topic
     */
    public ReceiverOptions<String, Object> getKafkaOptions(String topicName){
        var consumerProperties = kafkaReceiverOptions.consumerProperties();
        if (!doesTopicExist(topicName, consumerProperties)) {
            throw new IllegalArgumentException("Topic does not exist: " + topicName);
        }
        var kafkaOptions = kafkaReceiverOptions.subscription(List.of(topicName));
        kafkaOptions.addAssignListener(partitions ->
                partitions.forEach(ReceiverPartition::seekToBeginning));
        return kafkaOptions;
    }


    public KafkaReceiver<String, RoleMessage> createKafkaRoleMessageReceiver(String topicName) {
        Map<String, Object> props = kafkaReceiverOptions.consumerProperties();
        if (!doesTopicExist(topicName, props)) {
            throw new IllegalArgumentException("Topic does not exist: " + topicName);
        }

        ReceiverOptions<String, RoleMessage> options = ReceiverOptions.<String, RoleMessage>create(props)
                .subscription(Collections.singletonList(topicName));
        // ELIMINAR esta línea que fuerza la lectura desde el inicio
        //options.addAssignListener(partitions ->
        //        partitions.forEach(ReceiverPartition::seekToBeginning));

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
