package com.walrex.module_almacen.common.Traces;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collection;

@Configuration
public class TracingConfig {
    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        ResourceAttributes.SERVICE_NAME, "module-almacen"
                )));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(createMdcSpanProcessor())
                .addSpanProcessor(createExportingSpanProcessor())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    private SpanProcessor createMdcSpanProcessor() {
        return new SpanProcessor() {
            @Override
            public void onStart(Context parentContext, ReadWriteSpan span) {
                SpanContext ctx = span.getSpanContext();
                if (ctx.isValid()) {
                    MDC.put("traceId", ctx.getTraceId());
                    MDC.put("spanId", ctx.getSpanId());
                }
            }

            @Override
            public void onEnd(ReadableSpan span) {
                // No action needed on end
            }

            @Override
            public boolean isStartRequired() {
                return true;
            }

            @Override
            public boolean isEndRequired() {
                return false;
            }
        };
    }

    private SpanProcessor createExportingSpanProcessor() {
        // Para entornos sin sistema de tracing centralizado,
        // podemos exportar a archivos
        SpanExporter fileExporter = SpanExporter.composite(new FileSpanExporter());
        return BatchSpanProcessor.builder(fileExporter).build();
    }

    // Exportador de trazas a archivos
    public static class FileSpanExporter implements SpanExporter {
        private static final Logger log = LoggerFactory.getLogger(FileSpanExporter.class);
        private final Path tracesPath = Path.of("traces");

        public FileSpanExporter() {
            try {
                Files.createDirectories(tracesPath);
            } catch (IOException e) {
                log.error("No se pudo crear directorio de trazas", e);
            }
        }

        @Override
        public CompletableResultCode export(Collection<SpanData> spans) {
            if (spans.isEmpty()) {
                return CompletableResultCode.ofSuccess();
            }

            try {
                // Formato simple para trazas
                String timestamp = Instant.now().toString().replace(":", "-");
                Path file = tracesPath.resolve("trace-" + timestamp + ".json");

                try (BufferedWriter writer = Files.newBufferedWriter(
                        file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    ObjectMapper mapper = new ObjectMapper();
                    for (SpanData span : spans) {
                        writer.write(mapper.writeValueAsString(span));
                        writer.newLine();
                    }
                }
                return CompletableResultCode.ofSuccess();
            } catch (Exception e) {
                log.error("Error al exportar trazas", e);
                return CompletableResultCode.ofFailure();
            }
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    }
}