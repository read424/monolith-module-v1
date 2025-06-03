package com.walrex.user.module_users.infrastructure.adapters.outbound.producer;

import com.walrex.avro.schemas.PasswordRecoveryEvent;
import com.walrex.user.module_users.application.ports.output.PasswordRecoveryProducer;
import com.walrex.user.module_users.domain.model.PasswordRecoveryData;
import com.walrex.user.module_users.infrastructure.adapters.outbound.producer.mapper.PasswordRecoveryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class KafkaPasswordRecoveryProducer implements PasswordRecoveryProducer {
    private final KafkaSender<String, PasswordRecoveryEvent> kafkaSender;
    private final PasswordRecoveryMapper mapper;

    // Configuración inyectada desde propiedades
    @Value("${kafka.topics.password-recovery:email-requests-topic}")
    private String passwordRecoveryTopic;

    @Value("${app.mail.templates.recovery:reset-password.html}")
    private String defaultTemplate;

    // Constructor explícito con @Qualifier
    public KafkaPasswordRecoveryProducer(
            @Qualifier("userModuleAvroKafkaSender") KafkaSender<String, PasswordRecoveryEvent> kafkaSender,
            PasswordRecoveryMapper mapper) {
        this.kafkaSender = kafkaSender;
        this.mapper = mapper;
    }
    @Override
    public Mono<Void> sendPasswordRecoveryEvent(PasswordRecoveryData recoveryData) {
        if (recoveryData == null) {
            return Mono.error(new IllegalArgumentException("RecoveryData no puede ser nulo"));
        }
        return Mono.fromCallable(()-> mapper.toPasswordRecoveryEvent(recoveryData, defaultTemplate))
                .flatMap(event ->{
                    ProducerRecord<String, PasswordRecoveryEvent> record = new ProducerRecord<>(passwordRecoveryTopic, recoveryData.getEmail(), event);
                    record.headers().add("eventType", "PASSWORD_RECOVERY".getBytes(StandardCharsets.UTF_8));
                    return kafkaSender.send(Mono.just(SenderRecord.create(record, null)))
                            .doOnNext(result-> log.info("✅ Evento enviado: topic={}, offset={}", result.recordMetadata().topic(), result
                                    .recordMetadata().offset()))
                            .then();
                });
    }
}
