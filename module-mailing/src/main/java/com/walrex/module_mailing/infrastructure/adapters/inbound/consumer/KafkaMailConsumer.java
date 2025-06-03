package com.walrex.module_mailing.infrastructure.adapters.inbound.consumer;

import com.walrex.avro.schemas.PasswordRecoveryEvent;
import com.walrex.module_mailing.application.ports.input.MailUseCase;
import com.walrex.module_mailing.config.kafka.consumer.factory.MailingKafkaConsumerFactory;
import com.walrex.module_mailing.domain.model.MailMessage;
import com.walrex.module_mailing.infrastructure.adapters.inbound.consumer.mapper.PasswordRecoveryEventMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMailConsumer {
    private final MailingKafkaConsumerFactory kafkaFactory;
    private final MailUseCase mailUseCase;
    private final PasswordRecoveryEventMapper recoveryMapper;

    @PostConstruct
    public void startListeningForMailRequests() {
        KafkaReceiver<String, PasswordRecoveryEvent> kafkaReceiver =
                kafkaFactory.createReceiver("email-requests-topic");
        kafkaReceiver.receive()
                .doOnNext(record -> log.info("📧 Mail request recibido para: {}", record.value().getEmail()))
                .flatMap(record ->{
                    Object message_recovery = record.value();
                    if(message_recovery==null){
                        return Mono.empty();
                    }
                    if(!(message_recovery instanceof PasswordRecoveryEvent)){
                        return Mono.empty();
                    }
                    MailMessage mailMessage = (MailMessage) recoveryMapper.toMailMessage(record.value());
                    return mailUseCase.sendMail(mailMessage)
                            .doOnSuccess(v -> {
                                log.info("✅ Mail procesado correctamente");
                                record.receiverOffset().acknowledge();
                            })
                            .onErrorResume(e -> {
                                log.error("❌ Error procesando mail: {}", e.getMessage(), e);
                                // Decisión sobre acknowledgement según política de reintentos
                                return Mono.empty();
                            });
                })
                .subscribe();
    }
}
