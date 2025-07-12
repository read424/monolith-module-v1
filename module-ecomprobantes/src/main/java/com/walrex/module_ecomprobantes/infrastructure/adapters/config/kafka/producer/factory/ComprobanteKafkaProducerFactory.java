package com.walrex.module_ecomprobantes.infrastructure.adapters.config.kafka.producer.factory;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.concurrent.TimeUnit;

@Component("comprobanteKafkaProducerFactory")
@Slf4j
public class ComprobanteKafkaProducerFactory {
    private final SenderOptions<String, Object> kafkaProducerOptions;

    public ComprobanteKafkaProducerFactory(
        @Qualifier("comprobanteModuleProducerOptions") SenderOptions<String, Object> kafkaProducerOptions
    ) {
        this.kafkaProducerOptions = kafkaProducerOptions;
    }

    @Bean(name="comprobanteCreateAvroSender")
    public <T> KafkaSender<String, T> productCreateAvroSender() {
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
