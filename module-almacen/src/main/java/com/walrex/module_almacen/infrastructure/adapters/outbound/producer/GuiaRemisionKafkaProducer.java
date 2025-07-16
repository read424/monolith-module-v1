package com.walrex.module_almacen.infrastructure.adapters.outbound.producer;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.CreateGuiaRemisionRemitenteMessage;
import com.walrex.module_almacen.application.ports.output.EnviarGuiaRemisionEventPort;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDataDTO;
import com.walrex.module_almacen.domain.model.enums.TypeComprobante;
import com.walrex.module_almacen.infrastructure.adapters.outbound.producer.mapper.GuiaRemisionDataDTOMapperAvro;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Adaptador que implementa el puerto de salida para enviar eventos de guía de
 * remisión
 * utilizando Kafka como mecanismo de transporte con circuit breaker.
 */
@Component
@Slf4j
public class GuiaRemisionKafkaProducer implements EnviarGuiaRemisionEventPort {

        private final KafkaSender<String, Object> kafkaSender;
        private final GuiaRemisionDataDTOMapperAvro guiaRemisionDTOMapperAvro;
        @Value("${kafka.topics.almacen.create-comprobante-guia-remision}")
        private String guiaRemisionTopic;

        public GuiaRemisionKafkaProducer(
                        @Qualifier("almacenCreateAvroSender") KafkaSender<String, Object> kafkaSender,
                        GuiaRemisionDataDTOMapperAvro guiaRemisionDTOMapperAvro) {
                this.kafkaSender = kafkaSender;
                this.guiaRemisionDTOMapperAvro = guiaRemisionDTOMapperAvro;
        }

        @Override
        @CircuitBreaker(name = "guiaRemisionKafkaProducer", fallbackMethod = "fallbackEnviarEvento")
        public Mono<Void> enviarEventoGuiaRemision(GuiaRemisionGeneradaDataDTO guiaRemisionGenerada,
                        String correlationId, Boolean isComprobanteSUNAT) {
                MDC.put("correlationId", correlationId);

                log.info("📤 Preparando envío de evento guía de remisión. CorrelationId: {}, OrdenSalida: {}, Topic: {}",
                                correlationId, guiaRemisionGenerada, guiaRemisionTopic);

                return Mono.just(guiaRemisionDTOMapperAvro.toAvro(guiaRemisionGenerada))
                                .doOnNext(message -> {
                                        message.setTipoComprobante(TypeComprobante.GUIA_REMISION_REMITENTE_SUNAT
                                                        .getId_comprobante());
                                        if (isComprobanteSUNAT) {
                                                message.setTipoSerie(TypeComprobante.GUIA_REMISION_REMITENTE_SUNAT
                                                                .getId_serie());
                                        } else {
                                                message.setTipoSerie(
                                                                TypeComprobante.GUIA_REMISION_REMITENTE.getId_serie());
                                        }
                                        log.debug(
                                                        "✅ Mensaje Avro generado para orden: {}, cliente: {}, items: {}",
                                                        guiaRemisionGenerada, message.getIdCliente(),
                                                        message.getDetailItems().size());
                                })
                                .flatMap(avroMessage -> enviarMensajeKafka(avroMessage, correlationId,
                                                guiaRemisionGenerada.getIdOrdenSalida(),
                                                guiaRemisionGenerada.getIdUsuario()))
                                .doOnSuccess(v -> log.info(
                                                "✅ Evento de guía de remisión enviado exitosamente. CorrelationId: {}, OrdenSalida: {}",
                                                correlationId, guiaRemisionGenerada.getIdOrdenSalida()))
                                .doOnError(error -> log.error(
                                                "❌ Error al enviar evento de guía de remisión. CorrelationId: {}, OrdenSalida: {}, Error: {}",
                                                correlationId, guiaRemisionGenerada.getIdOrdenSalida(),
                                                error.getMessage()))
                                .doFinally(signalType -> {
                                        log.debug("🔄 Finalizando envío de evento con señal: {}. CorrelationId: {}",
                                                        signalType,
                                                        correlationId);
                                        MDC.remove("correlationId");
                                });
        }

        private Mono<Void> enviarMensajeKafka(CreateGuiaRemisionRemitenteMessage avroMessage, String correlationId,
                        Integer idOrdenSalida, Integer idUsuario) {
                try {
                        // Crear headers del mensaje
                        List<Header> headers = crearHeaders(correlationId, Long.valueOf(idOrdenSalida), idUsuario);

                        // Crear record de productor
                        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
                                        guiaRemisionTopic,
                                        correlationId, // Key para particionado
                                        avroMessage);

                        // Agregar headers al record
                        headers.forEach(header -> producerRecord.headers().add(header));

                        log.debug("📨 Enviando mensaje a Kafka. Topic: {}, Key: {}, Headers: {}",
                                        guiaRemisionTopic, correlationId, headers.size());

                        return kafkaSender.send(Mono.just(SenderRecord.create(producerRecord, correlationId)))
                                        .doOnNext(result -> log.info(
                                                        "✅ Mensaje enviado exitosamente a topic [{}], offset: {}, partition: {}, CorrelationId: {}",
                                                        guiaRemisionTopic,
                                                        result.recordMetadata().offset(),
                                                        result.recordMetadata().partition(),
                                                        correlationId))
                                        .doOnError(error -> log.error(
                                                        "❌ Error al enviar mensaje a Kafka topic [{}]: {}",
                                                        guiaRemisionTopic, error.getMessage(), error))
                                        .then();

                } catch (Exception e) {
                        log.error("❌ Error al preparar mensaje para envío: {}", e.getMessage(), e);
                        return Mono.error(e);
                }
        }

        private List<Header> crearHeaders(String correlationId, Long idOrdenSalida, Integer idUsuario) {
                List<Header> headers = new ArrayList<>();
                String messageId = UUID.randomUUID().toString();

                headers.add(new RecordHeader("correlationId", correlationId.getBytes(StandardCharsets.UTF_8)));
                headers.add(new RecordHeader("idusuario", idUsuario.toString().getBytes(StandardCharsets.UTF_8)));
                headers.add(new RecordHeader("messageId", messageId.getBytes(StandardCharsets.UTF_8)));
                headers.add(new RecordHeader("timestamp",
                                String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)));
                headers.add(new RecordHeader("source", "module-almacen".getBytes(StandardCharsets.UTF_8)));
                headers.add(new RecordHeader("event-type", "create-guia-remision".getBytes(StandardCharsets.UTF_8)));
                headers.add(new RecordHeader("id-orden-salida",
                                idOrdenSalida.toString().getBytes(StandardCharsets.UTF_8)));

                return headers;
        }

        /**
         * Método de fallback que se ejecuta cuando el circuit breaker está abierto
         */
        private Mono<Void> fallbackEnviarEvento(Long idOrdenSalida, String correlationId, Exception ex) {
                log.error("🔴 Circuit breaker activado para guía de remisión. CorrelationId: {}, OrdenSalida: {}, Error: {}",
                                correlationId, idOrdenSalida, ex.getMessage());

                // En un escenario real, aquí podrías:
                // 1. Guardar el evento en una tabla de eventos pendientes
                // 2. Enviar a un Dead Letter Queue
                // 3. Notificar a un sistema de alertas

                return Mono.error(new RuntimeException(
                                "Servicio Kafka no disponible temporalmente. Evento guardado para reintento.", ex));
        }
}