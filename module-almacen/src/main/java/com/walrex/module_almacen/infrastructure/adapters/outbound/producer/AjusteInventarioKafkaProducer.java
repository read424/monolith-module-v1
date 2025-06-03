package com.walrex.module_almacen.infrastructure.adapters.outbound.producer;

import com.walrex.avro.schemas.AjustInventaryResponseMessage;
import com.walrex.module_almacen.application.ports.output.EnviarRespuestaAjusteInventarioPort;
import com.walrex.module_almacen.domain.model.dto.ResponseAjusteInventoryDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.producer.mapper.AvroAjusteResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador que implementa el puerto de salida para enviar respuestas
 * utilizando Kafka como mecanismo de transporte.
 */
@Component
@Slf4j
public class AjusteInventarioKafkaProducer implements EnviarRespuestaAjusteInventarioPort {
    private final KafkaSender<String, Object> kafkaSender;
    private final AvroAjusteResponseMapper inventarioMapper;

    public AjusteInventarioKafkaProducer(
            @Qualifier("almacenCreateAvroSender") KafkaSender<String, Object> kafkaSender,
            AvroAjusteResponseMapper inventarioMapper
    ){
        this.kafkaSender=kafkaSender;
        this.inventarioMapper=inventarioMapper;
    }

    @Value("${kafka.topics.almacen.create-ingreso-ajuste-response}")
    private String responseTopic;

    @Override
    public Mono<Void> enviarRespuesta(String correlationId, ResponseAjusteInventoryDTO respuesta) {
        MDC.put("correlationId", correlationId);

        log.info("Preparando envío de respuesta de ajuste. CorrelationId: {}, Topic: {}, Éxito: {}",
                correlationId, responseTopic, respuesta.isSuccess());

        if (log.isDebugEnabled()) {
            log.debug("Contenido de respuesta a enviar: TransactionId: {}, Mensaje: {}, Ingresos: {}, Egresos: {}",
                    respuesta.getTransactionId(),
                    respuesta.getMessage(),
                    respuesta.getResult_ingresos() != null ? respuesta.getResult_ingresos().getNum_saved() : 0,
                    respuesta.getResult_egresos() != null ? respuesta.getResult_egresos().getNum_saved() : 0);
        }
        try{
            log.debug("Transformando respuesta a formato Avro. CorrelationId: {}", correlationId);
            AjustInventaryResponseMessage avroMessage = inventarioMapper.toAvroResponse(respuesta);

            List<Header> headers = new ArrayList<>();
            headers.add(new RecordHeader("correlationId", correlationId.getBytes(StandardCharsets.UTF_8)));

            ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(responseTopic, avroMessage);
            // Agregar headers si se proporciona un correlationId
            if (correlationId != null && !correlationId.isEmpty()) {
                log.debug("Agregando header correlationId: {} a mensaje Kafka", correlationId);
                producerRecord.headers().add(new RecordHeader("correlationId", correlationId.getBytes()));
            }else{
                log.warn("Enviando mensaje sin correlationId al topic: {}", responseTopic);
            }
            log.debug("Iniciando envío de mensaje a Kafka. Topic: {}, CorrelationId: {}",
                    responseTopic, correlationId);
            return kafkaSender.send(Mono.just(SenderRecord.create(producerRecord, correlationId)))
                    .doOnNext(result -> {
                        log.info("Mensaje enviado exitosamente a topic [{}], offset: {}, partition: {}, CorrelationId: {}",
                                responseTopic,
                                result.recordMetadata().offset(),
                                result.recordMetadata().partition(),
                                correlationId);
                    })
                    .doOnError(error -> {
                        log.error("Error al enviar mensaje a Kafka topic [{}]: {}",
                                responseTopic, error.getMessage(), error);
                    })
                    .then()
                    .doFinally(signalType -> {
                        log.debug("Finalizando operación de envío de mensaje con señal: {}. CorrelationId: {}",
                                signalType, correlationId);
                        MDC.remove("correlationId");
                    });
        } catch (Exception e) {
            log.error("Error al preparar mensaje para envío: {}", e.getMessage(), e);
            MDC.remove("correlationId");
            return Mono.error(e);
        }
    }
}
