package com.walrex.module_almacen.common.kafka.consumer.factory;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component("almacenModuleKafkaConsumerFactory")
@Slf4j
public class AlmacenKafkaReceiverFactory {
    private final ReceiverOptions<String, Object> kafkaReceiverOptions;

    // Configuración para la creación de topics
    @Value("${kafka.topic.auto-create:true}") // Valor predeterminado true
    private boolean autoCreateTopics;

    @Value("${kafka.topic.partitions:1}") // Valor predeterminado 1
    private int partitions;

    @Value("${kafka.topic.replication-factor:1}") // Valor predeterminado 1
    private short replicationFactor;

    // Constructor con @Qualifier
    public AlmacenKafkaReceiverFactory(@Qualifier("almacenModuleReceiveOptions") ReceiverOptions<String, Object> kafkaReceiverOptions) {
        this.kafkaReceiverOptions = kafkaReceiverOptions;
    }

    public <T> KafkaReceiver<String, T> createReceiver(String topicName){
        Map<String, Object> props = new HashMap<>(kafkaReceiverOptions.consumerProperties());

        // Verificar si el topic existe y crearlo si es necesario
        ensureTopicExists(topicName, props);

        ReceiverOptions<String, T> options =
                ReceiverOptions.<String, T>create(props)
                        .subscription(Collections.singletonList(topicName));
        return KafkaReceiver.create(options);
    }

    /**
     * Verifica si un topic existe y lo crea si es necesario
     * @param topicName Nombre del topic a verificar/crear
     * @param properties Propiedades del consumidor de Kafka
     */
    private void ensureTopicExists(String topicName, Map<String, Object> properties) {
        try (AdminClient admin = AdminClient.create(properties)) {
            boolean topicExists = admin.listTopics().names().get().contains(topicName);

            if (!topicExists) {
                if (autoCreateTopics) {
                    // Crear el topic si no existe y la auto-creación está habilitada
                    NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
                    admin.createTopics(Collections.singleton(newTopic)).all().get();
                    log.info("Topic creado automáticamente: {}", topicName);
                } else {
                    // Si la auto-creación está deshabilitada, lanzar excepción
                    throw new IllegalArgumentException("Topic does not exist: " + topicName);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            // Manejar excepciones específicas de la creación
            throw new RuntimeException("Error al verificar/crear el topic: " + topicName, e);
        } catch (Exception e) {
            // Manejar otras excepciones
            throw new RuntimeException("Error inesperado al acceder a Kafka: " + e.getMessage(), e);
        }
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
