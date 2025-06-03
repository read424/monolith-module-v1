package com.walrex.module_almacen.infrastructure.adapters.inbound.consumer;

import com.walrex.avro.schemas.AjustInventaryMessage;
import com.walrex.module_almacen.application.ports.input.ProcesarAjusteInventarioUseCase;
import com.walrex.module_almacen.application.ports.output.EnviarRespuestaAjusteInventarioPort;
import com.walrex.module_almacen.common.kafka.consumer.factory.AlmacenKafkaReceiverFactory;
import com.walrex.module_almacen.common.kafka.producer.manager.AlmacenKafkaRequestReplyManager;
import com.walrex.module_almacen.domain.model.dto.RequestAjusteInventoryDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.consumer.mapper.InventarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenAjusteInventarioKafkaConsumer {
    private final AlmacenKafkaReceiverFactory kafkaReceiverFactory;
    private final AlmacenKafkaRequestReplyManager requestReplyManager;
    private final InventarioMapper inventarioMapper;
    private final ProcesarAjusteInventarioUseCase ajusteInventarioUseCase;
    private final EnviarRespuestaAjusteInventarioPort enviarRespuestaPort;

    @Value("${kafka.topics.almacen.create-ingreso-ajuste:create-ingreso-ajuste}")
    private String name_topic;

    public void startListening(){
        log.info("Iniciando consumidor Kafka para el topic: {}", name_topic);
        KafkaReceiver<String, AjustInventaryMessage> kafkaReceiver = kafkaReceiverFactory.createReceiver(name_topic);
        kafkaReceiver.receive()
                .doOnNext(record -> {
                    String correlationId = extractCorrelationId(record);
                    if(correlationId!=null){
                        MDC.put("correlationId", correlationId);
                        log.debug("Mensaje Kafka recibido con correlationId: {}", correlationId);
                    }else{
                        log.warn("Mensaje Kafka recibido sin correlationId");
                    }
                    if(log.isDebugEnabled()){
                        log.debug("Mensaje recibido de Kafka [topic: {}, partition: {}, offset: {}]: {}",
                                record.topic(), record.partition(), record.offset(), record.value());
                    }else{
                        log.info("Mensaje recibido de Kafka [topic: {}, partition: {}, offset: {}]",
                                record.topic(), record.partition(), record.offset());
                    }
                })
                .flatMap(record -> {
                    Object message_inventory = record.value();
                    if(message_inventory==null){
                        log.warn("Mensaje Kafka recibido con valor nulo");
                        record.receiverOffset().acknowledge();
                        return Mono.empty();
                    }
                    if(!(message_inventory instanceof  AjustInventaryMessage)){
                        log.error("Mensaje Kafka recibido con tipo incorrecto: {}",
                                message_inventory.getClass().getName());
                        record.receiverOffset().acknowledge();
                        return Mono.empty();
                    }
                    String correlationId = extractCorrelationId(record);
                   if(correlationId==null){
                       log.warn("Mensaje Kafka sin correlationId, no se puede procesar");
                       record.receiverOffset().acknowledge();
                       return Mono.empty();
                   }
                   try{
                       //Convertir el mensaje Avro a objeto de dominio
                       AjustInventaryMessage avroResponse = (AjustInventaryMessage) message_inventory;
                       RequestAjusteInventoryDTO inventario = inventarioMapper.mapAvroToDto(avroResponse);
                       log.info("Procesando ajuste de inventario para correlationId: {}, almacén: {}, motivo: {}",
                               correlationId, inventario.getId_almacen(), inventario.getId_motivo());
                       return ajusteInventarioUseCase.procesarAjusteInventario(inventario, correlationId)
                               .flatMap(respuesta->{
                                   log.debug("Ajuste procesado, enviando respuesta para correlationId: {}", correlationId);
                                   return enviarRespuestaPort.enviarRespuesta(correlationId, respuesta)
                                           .doOnSuccess(v-> log.info("Respuesta enviada exitosamente para correlationId: {}, éxito: {}",
                                                   correlationId, respuesta.isSuccess()))
                                           .doOnError(error -> log.error("Error al enviar respuesta para correlationId: {}: {}",
                                                   correlationId, error.getMessage(), error))
                                           .thenReturn(respuesta);
                               })
                               .doFinally(signalType -> {
                                   log.debug("Finalizando procesamiento para correlationId: {}, signal: {}",
                                           correlationId, signalType);
                                   record.receiverOffset().acknowledge();
                                   MDC.remove("correlationId");
                               })
                               .onErrorResume(error -> {
                                   log.error("Error al procesar ajuste para correlationId: {}: {}", correlationId, error.getMessage(), error);
                                   record.receiverOffset().acknowledge();
                                   MDC.remove("correlationId");
                                   return Mono.empty();
                               });
                   }catch(Exception e){
                       log.error("Excepción al procesar mensaje para correlationId: {}: {}",
                               correlationId, e.getMessage(), e);
                       requestReplyManager.removeRequestWithError(correlationId, "Error procesando respuesta" + e.getMessage());
                       record.receiverOffset().acknowledge();
                       MDC.remove("correlationId");
                       return Mono.empty();
                   }
                })
                .doOnError(error -> {
                    log.error("Error en el consumer de Kafka: {}", error.getMessage(), error);
                    MDC.remove("correlationId");
                })
                .retryWhen(Retry.indefinitely()
                        .doBeforeRetry(retrySignal ->
                                log.warn("Reintentando conexión a Kafka después de error. Intento #{}",
                                        retrySignal.totalRetries() + 1))
                )
                .subscribe();
        log.info("Consumidor Kafka iniciado correctamente para el topic: {}", name_topic);
    }

    private String extractCorrelationId(ReceiverRecord<String, AjustInventaryMessage> record) {
        return StreamSupport.stream(record.headers().spliterator(), false)
                .filter(header -> header.key().equals("correlationId"))
                .map(header -> new String(header.value(), StandardCharsets.UTF_8))
                .findFirst()
                .orElse(null);
    }
}
