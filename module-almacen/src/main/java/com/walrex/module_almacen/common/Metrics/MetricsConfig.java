package com.walrex.module_almacen.common.Metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        // Crear el registro de métricas
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Configurar tags comunes
        registry.config().commonTags("module", "module-almacen");

        // Si no quieres un servidor web, puedes exportar métricas a un archivo
        FileMetricsPublisher filePublisher = new FileMetricsPublisher();
        registry.config().meterFilter(MeterFilter.acceptNameStartsWith("kafka"));
        registry.config().meterFilter(MeterFilter.acceptNameStartsWith("reactor"));

        return registry;
    }

    @Bean
    public KafkaMetrics kafkaMetrics(MeterRegistry registry, @Value("${spring.kafka.bootstrap-servers}") String kafkaProperties) {
        // Registrar métricas específicas de Kafka
        return new KafkaMetrics(kafkaProperties, registry);
    }

    // Clase para publicar métricas en archivo
    @RequiredArgsConstructor
    public static class FileMetricsPublisher {
        private static final Logger log = LoggerFactory.getLogger(FileMetricsPublisher.class);
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final Path metricsPath = Path.of("metrics", "prometheus");

        public void start(PrometheusMeterRegistry registry) {
            try {
                Files.createDirectories(metricsPath);
            } catch (IOException e) {
                log.error("No se pudo crear directorio de métricas", e);
                return;
            }

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    String metrics = registry.scrape();
                    Files.writeString(
                            metricsPath.resolve("metrics.prom"),
                            metrics,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    );
                } catch (Exception e) {
                    log.error("Error al escribir métricas", e);
                }
            }, 0, 15, TimeUnit.SECONDS);

            // Registrar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }));
        }
    }
}
