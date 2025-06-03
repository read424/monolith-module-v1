package com.walrex.role.module_role.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@TestPropertySource(locations = "classpath:application-test.yml")
public class KafkaTestContainer {
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:latest");
    private static KafkaContainer kafkaContainer;

    @Value("${spring.kafka.bootstrap-servers}")
    private static String bootstrapServers;

    private KafkaTestContainer() {
        // Evitar instancias directas
    }

    public static KafkaContainer getInstance() {
        if (kafkaContainer == null) {
            kafkaContainer = new KafkaContainer(KAFKA_IMAGE);
            kafkaContainer.start();
        }
        return kafkaContainer;
    }

    public static String getBootstrapServers() {
        return kafkaContainer.getBootstrapServers();
    }
}
