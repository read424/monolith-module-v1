package com.walrex.module_articulos.infrastructure.adapters.inbound.consumer;

import com.walrex.avro.schemas.GetCodesArticulosEvents;
import com.walrex.module_articulos.application.ports.input.GetArticulosUseCase;
import com.walrex.module_articulos.config.kafka.consumer.factory.ProductKafkaReceiverFactory;
import com.walrex.module_articulos.infrastructure.adapters.inbound.consumer.mapper.AvroMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaArticuloConsumer {
    @Qualifier("productModuleKafkaConsumerFactory")
    private final ProductKafkaReceiverFactory kafkaReceiverFactory;
    private final GetArticulosUseCase articuloUseCase;
    private final AvroMapper avroMapper;
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @Value("${kafka.topics.get-info-articulos:articulo-by-code}")
    private String articuloTopic;

    @PostConstruct
    public void listenToKafka(){
        try{
            log.info("📥 Topic {}", articuloTopic);
            KafkaReceiver<String, GetCodesArticulosEvents> kafkaReceiver = kafkaReceiverFactory.createReceiver(articuloTopic);

            // Crear un circuit breaker específico
            ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("articleProducerKafka");

            kafkaReceiver.receive()
                    .doOnNext(record -> log.info("message recibido para: {} {}", articuloTopic, record.value()))
                    .flatMap(record -> {
                        Object message_articles = record.value();
                        if(message_articles==null){
                            return Mono.empty();
                        }
                        if(!(message_articles instanceof GetCodesArticulosEvents)){
                            return Mono.empty();
                        }
                        GetCodesArticulosEvents event = (GetCodesArticulosEvents) message_articles;
                        Headers headers = record.headers();
                        String correlationId = Optional.ofNullable(headers.lastHeader("correlationId"))
                                .map(Header::value)
                                .map(String::new)
                                .orElse(null);
                        log.info("📩 CorrelationId recibido: {}", correlationId);
                        // Convertir evento Avro a lista de códigos
                        List<String> codigos = avroMapper.mapToCodigos(event);
                        // Procesar con el caso de uso
                        return articuloUseCase.getArticulosByCodigos(codigos, correlationId)
                            .doOnSuccess(result -> log.info("✅ Procesamiento completado para {} artículos",
                                    result.getArticulos_ids().size()))
                            .doOnError(e -> log.error("❌ Error procesando artículos: {}", e.getMessage(), e))
                            .then();
                    })
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                        .doBeforeRetry(signal ->
                            log.warn("🔄 Reintentando procesamiento después de error: {}", signal.failure().getMessage())
                        )
                    )
                    .subscribe(
                        success -> {}, // Nada que hacer en onNext
                        error -> log.error("❌ Error fatal en el consumidor Kafka: {}", error.getMessage(), error),
                        () -> log.info("🛑 Consumidor Kafka finalizado")
                    );
        }catch(Exception e){
            log.error("Error al crearse KafkaReceiver {}", e.getMessage(), e);
        }
    }
}
