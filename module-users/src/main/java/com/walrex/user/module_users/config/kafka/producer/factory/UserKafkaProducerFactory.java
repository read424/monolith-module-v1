package com.walrex.user.module_users.config.kafka.producer.factory;


import com.walrex.avro.schemas.RoleMessage;
import com.walrex.user.module_users.config.kafka.producer.serializers.UserMessageSerializer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.apache.kafka.clients.admin.AdminClient;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.concurrent.TimeUnit;

@Component("userModuleKafkaProducerFactory")
@Slf4j
public class UserKafkaProducerFactory {
    private final SenderOptions<String, Object> kafkaProducerOptions;

    // Constructor con Qualifier específico
    public UserKafkaProducerFactory(@Qualifier("userModuleProducerOptions") SenderOptions<String, Object> kafkaProducerOptions) {
        this.kafkaProducerOptions = kafkaProducerOptions;
    }

    @Bean(name = "userModuleStringKafkaSender")
    public KafkaSender<String, String> userModuleStringKafkaSender() {
        SenderOptions<String, String> options = SenderOptions.<String, String>create(kafkaProducerOptions.producerProperties())
                .withValueSerializer(new StringSerializer());
        return KafkaSender.create(options);
    }

    @Bean(name = "userModuleRoleMessageKafkaSender")
    public KafkaSender<String, RoleMessage> userModuleRoleMessageKafkaSender() {
        UserMessageSerializer roleMessageSerializer = new UserMessageSerializer();
        roleMessageSerializer.configure(kafkaProducerOptions.producerProperties(), false); // Configura el serializador

        SenderOptions<String, RoleMessage> options = SenderOptions.<String, RoleMessage>create(kafkaProducerOptions.producerProperties())
                .withValueSerializer(roleMessageSerializer);
        return KafkaSender.create(options);
    }

    // Nuevo método genérico para crear KafkaSender con Avro
    @Bean(name = "userModuleAvroKafkaSender")
    public <T> KafkaSender<String, T> userModuleAvroKafkaSender() {
        // Usar directamente las opciones de productor ya configuradas
        SenderOptions<String, T> options = SenderOptions.<String, T>create(
                kafkaProducerOptions.producerProperties());
        return KafkaSender.create(options);
    }

    @PostConstruct
    public void checkKafkaConnection() {
        try {
            AdminClient adminClient = AdminClient.create(kafkaProducerOptions.producerProperties());
            adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
            log.info("✅ Conexión con Kafka establecida correctamente");
            adminClient.close();
        } catch (Exception e) {
            log.error("❌ No se pudo establecer conexión con Kafka: {}", e.getMessage(), e);
        }
    }

    public void validateTopicExists(String topicName){
        try (var admin = AdminClient.create(kafkaProducerOptions.producerProperties())) {
            if (!admin.listTopics().names().get().contains(topicName)) {
                throw new IllegalArgumentException("❌ Topic does not exist: " + topicName);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Error validating topic existence: " + topicName, e);
        }
    }
}
