package com.walrex.user.module_users.infrastructure.adapters.outbound.producer;

import com.walrex.avro.schemas.RoleMessage;
import com.walrex.user.module_users.application.ports.output.RoleMessageProducer;
import com.walrex.user.module_users.config.kafka.producer.manager.UserKafkaRequestReplyManager;
//import io.github.resilience4j.circuitbreaker.CircuitBreaker;
//import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
//import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaRoleMessageProducer implements RoleMessageProducer {
    @Qualifier("roleMessageKafkaSender") // Especifica el nombre del bean
    private final KafkaSender<String, RoleMessage> kafkaSender;
    private final UserKafkaRequestReplyManager requestReplyManager;
    //private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @Override
    public Mono<String> sendMessage(String topic, String key, String message) {
        String correlationId = UUID.randomUUID().toString();
        RoleMessage roleMessage;
        try{
            // Intentamos convertir el mensaje a Long
            Long roleId = Long.valueOf(message);
            roleMessage = new RoleMessage(roleId);
        }catch(NumberFormatException e){
            log.error("❌ El mensaje no es un número válido: {}", message);
            return Mono.error(new IllegalArgumentException("El mensaje debe ser un número válido para RoleMessage"));
        }
        Mono<String> responseMono = requestReplyManager.registerRequest(correlationId)
                .timeout(Duration.ofSeconds(5)) // Si no hay respuesta en 5 segundos, lanza TimeoutException
                .onErrorResume(TimeoutException.class, ex -> {
                    log.error("⏳ Timeout esperando respuesta de module-rol para correlationId {}", correlationId);
                    requestReplyManager.removeRequest(correlationId); // Limpiamos la solicitud
                    return Mono.error(new RuntimeException("Timeout esperando respuesta de module-rol"));
                });
        ProducerRecord<String, RoleMessage> record = new ProducerRecord<>(topic, key, roleMessage);
        record.headers().add("correlationId", correlationId.getBytes(StandardCharsets.UTF_8));

        Mono<String> sendAndReceiveMono = kafkaSender.send(Mono.just(SenderRecord.create(record, null)))
                .doOnNext(result->log.info("📤 Mensaje enviado a {} con correlación {}: {}", topic, correlationId, message))
                .doOnError(e -> {
                    log.error("❌ Error al enviar mensaje: {}", e.getMessage());
                    requestReplyManager.removeRequestWithError(correlationId, "Error al enviar mensaje: " + e.getMessage());
                }).then(responseMono);

        return circuitBreakerFactory.create("kafkaProducer")
                .run(sendAndReceiveMono, throwable -> {
                    log.error("⛔ Circuit breaker activado: {}", throwable.getMessage());
                    requestReplyManager.removeRequestWithError(correlationId,
                            "Error al procesar la solicitud (circuit breaker): " + throwable.getMessage());
                    return Mono.error(new RuntimeException("Servicio no disponible temporalmente", throwable));
                });
        // Crear el circuit breaker programáticamente en lugar de usar la anotación
        /*
        //CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("kafkaProducer");
        return Mono.defer(() -> kafkaSender.send(Mono.just(SenderRecord.create(record, null)))
                        .doOnNext(result -> log.info("📤 Mensaje enviado a {} con correlación {}: {}", topic, correlationId, message))
                        .doOnError(e ->{
                            handleError(correlationId, "❌ Error al enviar mensaje: " + e.getMessage());
                            requestReplyManager.removeRequestWithError(correlationId, "Error al enviar mensaje: " + e.getMessage());
                        })
                        .then(responseMono) // Esperar la respuesta con timeout
                )
                //.transformDeferred(CircuitBreakerOperator.of(circuitBreaker)) // Aplicar CircuitBreaker a la espera de respuesta
                .onErrorResume(ex -> {
                    log.error("⛔ Error en la operación: {}", ex.getMessage(), ex);
                    requestReplyManager.removeRequestWithError(correlationId, "Error al procesar la solicitud: " + ex.getMessage());
                    return Mono.error(new RuntimeException("Error al procesar la solicitud: " + ex.getMessage(), ex));
                });
        */
    }

    private Mono<String> handleError(String correlationId, String message) {
        log.error(message);
        requestReplyManager.completeRequest(correlationId, "⚠️ " + message);
        return Mono.just("⚠️ " + message);
    }
}
