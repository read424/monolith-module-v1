package com.walrex.role.module_role.infrastructure.adapters.inbound.consumer;

import com.walrex.role.module_role.infrastructure.config.KafkaTestContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class KafkaRolConsumerIT {

    private static final String TOPIC = "detail-rol";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaRolConsumer kafkaRolConsumer;

    @BeforeAll
    static void setup() {
        KafkaTestContainer.getInstance();  // Iniciar Kafka desde la clase compartida
    }

    @Test
    void testConsumeMessage() {
        // Mensaje simulado
        String message = "12345"; // ID del rol simulado

        // Enviar mensaje al topic de Kafka
        kafkaTemplate.send(TOPIC, message);

        // Esperar que el consumer lo procese (usando Awaitility)
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    verify(kafkaRolConsumer, times(1)).listenToKafka();
                });
    }
}
