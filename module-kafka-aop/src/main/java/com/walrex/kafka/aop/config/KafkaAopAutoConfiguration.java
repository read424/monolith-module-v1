package com.walrex.kafka.aop.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.walrex.kafka.aop.aspects.KafkaConsumerAspect;
import com.walrex.kafka.aop.factory.ReactiveKafkaConsumerFactory;
import com.walrex.kafka.aop.manager.KafkaMetricsManager;
import com.walrex.kafka.aop.serialization.AvroSerializationHelper;

import lombok.extern.slf4j.Slf4j;
import reactor.kafka.receiver.KafkaReceiver;

/**
 * 🎭 Auto Configuración para Kafka AOP Module
 * 
 * Configuración automática que se activa cuando:
 * - reactor-kafka está en el classpath
 * - La propiedad kafka-aop.enabled=true (por defecto)
 * 
 * Funcionalidades:
 * - Habilita AspectJ automáticamente
 * - Registra todos los aspectos AOP
 * - Configura factories y helpers
 * - Escanea components del módulo
 * 
 * @author Kafka AOP Module
 */
@AutoConfiguration
@ConditionalOnClass({KafkaReceiver.class, org.aspectj.lang.ProceedingJoinPoint.class})
@ConditionalOnProperty(name = "kafka-aop.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KafkaAopProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = "com.walrex.kafka.aop")
@Slf4j
public class KafkaAopAutoConfiguration {

    public KafkaAopAutoConfiguration() {
        log.info("🎭 Kafka AOP Module - Configuración automática iniciada");
    }

    /**
     * 🏭 Factory para componentes reactivos de Kafka
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactiveKafkaConsumerFactory reactiveKafkaConsumerFactory() {
        log.info("✅ Configurando ReactiveKafkaConsumerFactory");
        return new ReactiveKafkaConsumerFactory();
    }

    /**
     * 📦 Helper para serialización Avro
     */
    @Bean
    @ConditionalOnMissingBean
    public AvroSerializationHelper avroSerializationHelper() {
        log.info("✅ Configurando AvroSerializationHelper");
        return new AvroSerializationHelper();
    }

    /**
     * 📊 Manager para métricas automáticas
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaMetricsManager kafkaMetricsManager() {
        log.info("✅ Configurando KafkaMetricsManager");
        return new KafkaMetricsManager();
    }

    /**
     * 🎯 Aspecto principal para KafkaConsumer
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaConsumerAspect kafkaConsumerAspect(
            ReactiveKafkaConsumerFactory consumerFactory,
            AvroSerializationHelper avroHelper,
            KafkaMetricsManager metricsManager) {
        log.info("✅ Configurando KafkaConsumerAspect");
        return new KafkaConsumerAspect(consumerFactory, avroHelper, metricsManager);
    }

    /**
     * 🎚️ Configuración condicional para desarrollo
     */
    @Bean
    @ConditionalOnProperty(name = "kafka-aop.dev-mode.enabled", havingValue = "true")
    public KafkaAopDevModeConfiguration devModeConfiguration(KafkaAopProperties properties) {
        log.info("🛠️ Configurando modo desarrollo para Kafka AOP");
        return new KafkaAopDevModeConfiguration(properties);
    }

    /**
     * 📈 Configuración condicional para métricas Prometheus
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.prometheus.PrometheusMeterRegistry")
    @ConditionalOnProperty(name = "kafka-aop.metrics.prometheus.enabled", havingValue = "true")
    public KafkaAopPrometheusConfiguration prometheusConfiguration() {
        log.info("📊 Configurando exportación de métricas a Prometheus");
        return new KafkaAopPrometheusConfiguration();
    }

    /**
     * 🧪 Configuración condicional para testing
     */
    @Bean
    @ConditionalOnProperty(name = "kafka-aop.test-mode.enabled", havingValue = "true")
    public KafkaAopTestConfiguration testConfiguration() {
        log.info("🧪 Configurando modo testing para Kafka AOP");
        return new KafkaAopTestConfiguration();
    }

    /**
     * 🛠️ Configuración para modo desarrollo
     */
    public static class KafkaAopDevModeConfiguration {
        private final KafkaAopProperties properties;

        public KafkaAopDevModeConfiguration(KafkaAopProperties properties) {
            this.properties = properties;
            log.info("🛠️ Kafka AOP Dev Mode habilitado - Logging extra: {}", 
                properties.getDevMode().isVerboseLogging());
        }

        // TODO: Agregar configuraciones específicas para desarrollo
        // - Mock de Schema Registry
        // - Embedded Kafka para testing
        // - Logging verbose automático
    }

    /**
     * 📊 Configuración para Prometheus
     */
    public static class KafkaAopPrometheusConfiguration {
        
        public KafkaAopPrometheusConfiguration() {
            log.info("📊 Kafka AOP Prometheus integration configurada");
        }

        // TODO: Agregar configuraciones específicas para Prometheus
        // - Custom meters para Kafka AOP
        // - Dashboards automáticos
        // - Alertas predefinidas
    }

    /**
     * 🧪 Configuración para testing
     */
    public static class KafkaAopTestConfiguration {
        
        public KafkaAopTestConfiguration() {
            log.info("🧪 Kafka AOP Test Mode configurado");
        }

        // TODO: Agregar configuraciones específicas para testing
        // - Mock automático de aspectos
        // - Test slices para Kafka AOP
        // - Utilities de testing
    }
} 