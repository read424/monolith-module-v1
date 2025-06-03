package com.walrex.module_almacen.common.Observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//@Configuration
public class ObservabilityConfig {
    @Value("${spring.application.name")
    String name_module;

    //@Bean
    public MeterRegistry meterRegistry(){
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        registry.config().commonTags("module", "module-almacen");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r->{
            Thread thread = new Thread(r, "metrics-export-scheduler");
            thread.setDaemon(true);
            return thread;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try{
                scheduler.shutdown();
                if(!scheduler.awaitTermination(10, TimeUnit.SECONDS)){
                    scheduler.shutdown();
                }
            }catch(InterruptedException e){
                scheduler.shutdown();
                Thread.currentThread().interrupt();
            }
        }));

        scheduler.scheduleAtFixedRate(()->exportMetrics(registry), 5,  15, TimeUnit.SECONDS);
        return registry;
    }

    private void exportMetrics(PrometheusMeterRegistry registry){
        try{
            String metrics = registry.scrape();
            WebClient.create("http://localhost:8081")
                    .post()
                    .uri("/api/metrics/collect")
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Module-Name", name_module)
                    .bodyValue(metrics)
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe();
        }catch(Exception e){

        }
    }

    //@Bean
    public OpenTelemetry openTelemetry(){
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, name_module
                )));

        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(
                        W3CTraceContextPropagator.getInstance()
                )).build();
    }
}
