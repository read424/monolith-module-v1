package com.walrex.user.module_users.infrastructure.adapters.inbound.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walrex.avro.schemas.RoleResponseMessage;
import com.walrex.user.module_users.domain.model.RolDetailDTO;
import com.walrex.user.module_users.infrastructure.adapters.inbound.consumer.mapper.UserRoleDetailMapper;
import com.walrex.user.module_users.config.kafka.consumer.factory.UserKafkaConsumerFactory;
import com.walrex.user.module_users.config.kafka.producer.manager.UserKafkaRequestReplyManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaRoleResponseListener {
    private final UserKafkaConsumerFactory kafkaFactory;
    private final UserKafkaRequestReplyManager requestReplyManager;
    private final UserRoleDetailMapper roleMapper;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void startListeningForResponses() {
        KafkaReceiver<String, RoleResponseMessage> kafkaReceiver = kafkaFactory.createReceiver("details-rol-response");
        kafkaReceiver.receive()
            .doOnNext(record -> {
                log.info("📥 Respuesta recibida: {}", record.value());
            })
            .flatMap(record ->{
                Object responseMessage = record.value();
                if (responseMessage == null) {
                    log.warn("⚠️ Mensaje recibido con valor nulo");
                    return Mono.empty();
                }
                if(!(responseMessage instanceof RoleResponseMessage)){
                    log.warn("⚠️ Mensaje recibido no es de tipo RoleResponseMessage");
                    return Mono.empty();
                }
                String correlationId = extractCorrelationId(record);
                if (correlationId == null) {
                    log.warn("⚠️ No se encontró correlationId en el mensaje");
                    return Mono.empty();
                }
                try{
                    RolDetailDTO responseDto =roleMapper.avroToRolDetailDto(record.value());
                    String jsonResponse = objectMapper.writeValueAsString(responseDto);
                    requestReplyManager.completeRequest(correlationId, jsonResponse);
                    log.info("✅ Respuesta procesada para correlationId: {}", correlationId);
                }catch(JsonProcessingException e){
                    log.error("Error al convertir respuesta a JSON: {}", e.getMessage(), e);
                    requestReplyManager.removeRequestWithError(correlationId, "Error procesando respuesta");
                }
                return Mono.empty();
            })
            .subscribe();
    }

    private String extractCorrelationId(ReceiverRecord<String, RoleResponseMessage> record) {
        return StreamSupport.stream(record.headers().spliterator(), false)
                .filter(header -> header.key().equals("correlationId"))
                .map(header -> new String(header.value(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);
    }
}
