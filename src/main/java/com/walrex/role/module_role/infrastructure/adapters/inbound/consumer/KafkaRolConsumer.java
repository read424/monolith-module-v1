package com.walrex.role.module_role.infrastructure.adapters.inbound.consumer;

import com.walrex.avro.schemas.RoleMessage;
import com.walrex.role.module_role.application.ports.input.RolDetailsUseCase;
import com.walrex.role.module_role.application.ports.output.RoleMessageProducer;
import com.walrex.role.module_role.config.kafka.consumer.factory.RoleKafkaReceiverFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.util.retry.Retry;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class KafkaRolConsumer {
    private final RoleKafkaReceiverFactory kafkaFactory;
    private final RolDetailsUseCase rolDetailsUseCase;
    private final RoleMessageProducer roleMessageProducer;

    @Value("${kafka.topics.rol-details:detail-rol}")
    private String roleDetailsTopic;

    // Constructor con Qualifier específico
    public KafkaRolConsumer(
            @Qualifier("roleModuleKafkaConsumerFactory") RoleKafkaReceiverFactory kafkaFactory,
            RolDetailsUseCase rolDetailsUseCase,
            RoleMessageProducer roleMessageProducer) {
        this.kafkaFactory = kafkaFactory;
        this.rolDetailsUseCase = rolDetailsUseCase;
        this.roleMessageProducer = roleMessageProducer;
    }

    @PostConstruct
    public void listenToKafka(){
        try{
            log.info("📥 Topic {}", roleDetailsTopic);
            KafkaReceiver<String, RoleMessage> kafkaReceiver = kafkaFactory.createKafkaRoleMessageReceiver(roleDetailsTopic);
            kafkaReceiver.receive()
                    .checkpoint("Antes de procesar mensaje")
                    .flatMap(record -> {
                        Object roleMessage = record.value();
                        if (roleMessage instanceof RoleMessage) {
                            log.warn("⚠️ If instanceof RoleMessage");
                            //RoleMessage roleMessage = (RoleMessage) record.value();
                            //Long idRol = roleMessage.getIdRol();
                            // resto del código...
                        }
                        if (roleMessage == null) {
                            log.warn("⚠️ Mensaje recibido con valor nulo");
                            return Mono.empty();
                        }
                        log.info("📥 Mensaje recibido: {}", roleMessage);
                        log.info("Tipo de objeto recibido: {}", roleMessage.getClass().getName());
                        if(roleMessage.getClass().getSimpleName().equals("RoleMessage")){
                            Headers headers = record.headers();
                            String correlationId = Optional.ofNullable(headers.lastHeader("correlationId"))
                                    .map(Header::value)
                                    .map(String::new)
                                    .orElse(null);

                            log.info("📩 CorrelationId recibido: {}", correlationId);
                            try{
                                Method getIdRolMethod = roleMessage.getClass().getMethod("getIdRol");
                                Long idRol = (Long) getIdRolMethod.invoke(roleMessage);
                                return processRoleMessageById(idRol, correlationId);
                            } catch (Exception e) {
                                log.error("Error accediendo a getIdRol: {}", e.getMessage(), e);
                                return Mono.empty();
                            }
                        }else{
                            log.error("❌ Mensaje recibido no es de tipo RoleMessage: {}", record.value().getClass().getName());
                            return Mono.empty();
                        }
                    })
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
                    .doOnError(e -> log.error("Error en Kafka Receiver: {}", e.getMessage(), e))
                    .doOnCancel(() -> log.warn("❌ Suscripción a Kafka cancelada"))
                    .doOnTerminate(() -> log.warn("🚫 KafkaReceiver terminado"))
                    .doOnNext(record -> {
                        System.out.println("📥 Mensaje recibido de Kafka: " + record.toString());
                    })
                    .subscribe();
        }catch(Exception e){
            log.error("Error al crearse KafkaReceiver {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa un mensaje de rol y envía la respuesta
     *
     * @param roleMessage El mensaje con la información del rol
     * @param correlationId ID de correlación para seguimiento
     * @return Mono<Void> que completa cuando el procesamiento finaliza
     */
    private Mono<Void> processRoleMessage(RoleMessage roleMessage, String correlationId) {
        Long idRol = roleMessage.getIdRol();

        return rolDetailsUseCase.getDetailsRolById(idRol)
                .doOnNext(roldetails -> log.info("📦 Detalles del rol obtenidos: {}", roldetails))
                .flatMap(roldetails -> {
                    // Enviar mensaje de respuesta usando el productor
                    return roleMessageProducer.sendMessage(
                            roldetails,
                            correlationId
                    );
                })
                .onErrorResume(e -> {
                    log.error("❌ Error procesando mensaje para rol {}: {}", idRol, e.getMessage(), e);
                    return Mono.empty();
                });
    }

    private Mono<Void> processRoleMessageById(Long idRol, String correlationId) {
        return rolDetailsUseCase.getDetailsRolById(idRol)
                .doOnNext(roldetails -> log.info("📦 Detalles del rol obtenidos: {}", roldetails))
                .flatMap(roldetails -> {
                    // Enviar mensaje de respuesta usando el productor
                    return roleMessageProducer.sendMessage(
                            roldetails,
                            correlationId
                    );
                })
                .onErrorResume(e -> {
                    log.error("❌ Error procesando mensaje para rol {}: {}", idRol, e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
