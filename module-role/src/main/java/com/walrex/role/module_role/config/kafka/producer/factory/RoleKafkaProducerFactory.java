package com.walrex.role.module_role.config.kafka.producer.factory;

import com.walrex.avro.schemas.RoleResponseMessage;
import com.walrex.role.module_role.config.kafka.producer.serializers.RoleResponseMessageSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Component("roleModuleKafkaProducerFactory")
@Slf4j
public class RoleKafkaProducerFactory {
    private final SenderOptions<String, Object> kafkaProducerOptions;

    // Constructor con Qualifier específico
    public RoleKafkaProducerFactory(@Qualifier("roleModuleProducerOptions") SenderOptions<String, Object> kafkaProducerOptions) {
        this.kafkaProducerOptions = kafkaProducerOptions;
    }

    @Bean(name = "roleModuleStringKafkaSender")
    public KafkaSender<String, String> roleModuleStringKafkaSender() {
        SenderOptions<String, String> options = SenderOptions.<String, String>create(kafkaProducerOptions.producerProperties())
                .withValueSerializer(new StringSerializer());
        return KafkaSender.create(options);
    }

    @Bean(name = "roleModuleResponseMessageKafkaSender")
    public KafkaSender<String, RoleResponseMessage> roleModuleResponseMessageKafkaSender(){
        RoleResponseMessageSerializer roleResponseMessageSerializer = new RoleResponseMessageSerializer();
        roleResponseMessageSerializer.configure(kafkaProducerOptions.producerProperties(), false);

        SenderOptions<String, RoleResponseMessage> options = SenderOptions.<String, RoleResponseMessage>create(kafkaProducerOptions.producerProperties())
                .withValueSerializer(roleResponseMessageSerializer);
        return KafkaSender.create(options);
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
