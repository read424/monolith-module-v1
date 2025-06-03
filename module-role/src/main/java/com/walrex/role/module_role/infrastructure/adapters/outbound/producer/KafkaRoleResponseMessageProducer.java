package com.walrex.role.module_role.infrastructure.adapters.outbound.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walrex.avro.schemas.RoleResponseMessage;
import com.walrex.role.module_role.application.ports.output.RoleMessageProducer;
import com.walrex.role.module_role.domain.model.RolDetailDTO;
import com.walrex.role.module_role.infrastructure.adapters.outbound.producer.mapper.RoleDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@Slf4j
public class KafkaRoleResponseMessageProducer implements RoleMessageProducer {

    private final KafkaSender<String, RoleResponseMessage> kafkaSender;
    private final RoleDetailMapper roleMapper;

    @Value("${kafka.topics.response-rol-details:details-rol-response}")
    private String roleDetailsTopic;

    // Constructor con Qualifier específico
    public KafkaRoleResponseMessageProducer(
            @Qualifier("roleModuleResponseMessageKafkaSender") KafkaSender<String, RoleResponseMessage> kafkaSender,
            RoleDetailMapper roleMapper) {
        this.kafkaSender = kafkaSender;
        this.roleMapper = roleMapper;
    }

    @Override
    public Mono<Void> sendMessage(RolDetailDTO message, String correlationId) {
        if (message == null) {
            log.error("❌ No se puede enviar un mensaje nulo");
            return Mono.error(new IllegalArgumentException("El mensaje no puede ser nulo"));
        }
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("🔄 Procesando mensaje para enviar: correlationId={}, mensaje={}",
                    correlationId, objectMapper.writeValueAsString(message));
        }catch(Exception e){
            log.info("🔄 Procesando mensaje para enviar: correlationId={}, id_rol={}, name_rol={}, detalles={}",
                    correlationId, message.getId_rol(), message.getName_rol(),
                    message.getDetails() != null ? message.getDetails().size() : 0);
        }
        // Validar que tengamos un ID de rol válido para la clave del mensaje
        String key = message.getId_rol() != null ? message.getId_rol().toString() : "unknown";

        return Mono.fromCallable(() -> {
                // Convertir el DTO a Avro
                RoleResponseMessage responseMessage = roleMapper.rolDetailDtoToAvro(message);
                return responseMessage;
            })
            .flatMap(responseMessage -> {
                log.debug("📦 Mensaje Avro creado: {}",  responseMessage.getNoRol());
                return kafkaSender.send(Mono.just(
                            SenderRecord.create(createProducerRecord(roleDetailsTopic, key, responseMessage, correlationId), null)
                    ))
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                            .maxBackoff(Duration.ofSeconds(5)))
                    .then();
            })
            .doOnNext(result -> log.info("📤 Mensaje Avro enviado a {}: id={}", roleDetailsTopic, message.getId_rol().toString()))
            .doOnError(e -> log.error("❌ Error al procesar/enviar mensaje: {}", e.getMessage(), e))
            .then();
    }

    private ProducerRecord<String, RoleResponseMessage> createProducerRecord(String topic, String key, RoleResponseMessage message, String correlationId) {
        ProducerRecord<String, RoleResponseMessage> producerRecord = new ProducerRecord<>(topic, key, message);
        if (correlationId != null) {
            producerRecord.headers().add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8));
            log.info("🔗 Agregado correlationId: {}", correlationId);
        }
        return producerRecord;
    }
}
