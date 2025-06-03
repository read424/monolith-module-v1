package com.walrex.module_articulos.infrastructure.adapters.outbound.producer;

import com.walrex.avro.schemas.ListArticuloIdsCodes;
import com.walrex.avro.schemas.ListArticulosIdsResponse;
import com.walrex.module_articulos.application.ports.output.ArticuloProducerOutputPort;
import com.walrex.module_articulos.domain.model.dto.ListArticulosDataDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KafkaArticuloProducer implements ArticuloProducerOutputPort {

    private final KafkaSender<String, Object> kafkaSender;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${kafka.topics.articulo.get-info-articulos-response:articulo-by-code-response}")
    private String responseTopic;

    public KafkaArticuloProducer(
            @Qualifier("productCreateAvroSender") KafkaSender<String, Object> kafkaSender,
            CircuitBreakerRegistry circuitBreakerRegistry
    ){
        this.kafkaSender=kafkaSender;
        this.circuitBreakerRegistry=circuitBreakerRegistry;
    }
    @Override
    public Mono<Void> sendArticulosResponse(ListArticulosDataDto response, String correlationId) {
        if (response == null) {
            log.error("❌ No se puede enviar un mensaje nulo");
            return Mono.error(new IllegalArgumentException("El mensaje no puede ser nulo"));
        }
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("kafkaProducer");

        return Mono.defer(()->{
            // Convertimos el DTO al objeto Avro
            List<ListArticuloIdsCodes> avroItems = response.getArticulos_ids().stream()
                    .map(item -> ListArticuloIdsCodes.newBuilder()
                            .setIdArticulo(item.getId_articulo())
                            .setCodigoArticulo(item.getCod_articulo())
                            .setDescArticulo(item.getDesc_articulo())
                            .build())
                    .collect(Collectors.toList());

            ListArticulosIdsResponse avroResponse = ListArticulosIdsResponse.newBuilder()
                    .setListArticulos(avroItems)
                    .build();

            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(responseTopic, avroResponse);
            // Agregar headers si se proporciona un correlationId
            if (correlationId != null && !correlationId.isEmpty()) {
                producerRecord.headers().add(new RecordHeader("correlationId", correlationId.getBytes()));
            }
            return kafkaSender.send(Mono.just(SenderRecord.create(producerRecord, null)))
                    .doOnNext(r -> log.info("🚀 Mensaje enviado a topic [{}]: {}", responseTopic, response))
                    .doOnError(e -> log.error("❌ Error al enviar mensaje a Kafka: {}", e.getMessage(), e))
                    .then();
        })
        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
        .onErrorResume(exception->{
            String errorMessage = "Error en la comunicación con kafka";
            if (exception instanceof CallNotPermittedException) {
                errorMessage = "Circuit breaker abierto - No se permiten más llamadas a Kafka temporalmente";
                log.warn("⚡ {}: {}", errorMessage, exception.getMessage());
            } else {
                log.error("⛔ {} con correlationId [{}]: {}", errorMessage, correlationId, exception.getMessage(), exception);
            }
            // Puedes decidir si quieres propagar el error o manejarlo aquí
            return Mono.error(new RuntimeException(errorMessage, exception));
        });
    }
}
