package com.walrex.kafka.aop.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 🎧 Application Listener para Kafka AOP Module
 * 
 * Se ejecuta cuando la aplicación está completamente inicializada
 * para realizar configuraciones finales y logging de estado.
 * 
 * @author Kafka AOP Module
 */
@Slf4j
public class KafkaAopApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("🎭 Kafka AOP Module - Inicialización completada");
        log.info("✅ AOP habilitado para annotations: @KafkaConsumer, @KafkaProducer, @KafkaRequestResponse");
        log.info("🔧 Configuración automática: ReceiverOptions, SenderOptions, Resilience4j");
        log.info("📦 Serialización Avro: Schema Registry integration activa");
        log.info("📊 Métricas automáticas: Consumer/Producer metrics habilitadas");
        log.info("🚀 Kafka AOP Module listo para usar!");
    }
} 