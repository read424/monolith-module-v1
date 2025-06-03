package com.walrex.user.module_users.infrastructure.adapters.outbound.producer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walrex.user.module_users.domain.model.UserEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
public class UserEventPublisher {
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;  // Para serializar JSON

    // Usando constructor con @Qualifier específico
    public UserEventPublisher(
            @Qualifier("userModuleStringKafkaSender") KafkaSender<String, String> kafkaSender,
            ObjectMapper objectMapper) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
    }


    public Mono<Void> publishUserEvent(UserEvent event){
        try{
            String jsonEvent = objectMapper.writeValueAsString(event);
            return kafkaSender.send(Mono.just(SenderRecord.create(
                            new ProducerRecord<>("user-events", event.getUserId(), jsonEvent),
                            null)))
                    .then();
        }catch(JsonProcessingException e){
            return Mono.error(e);
        }
    }
}
