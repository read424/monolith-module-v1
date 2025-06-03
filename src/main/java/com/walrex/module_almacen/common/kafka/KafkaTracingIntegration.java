package com.walrex.module_almacen.common.kafka;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.lang.Nullable;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaTracingIntegration {
    private final Tracer tracer;
    private final TextMapPropagator propagator;

    public KafkaTracingIntegration(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("kafka-tracing");
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
    }

    // Para producir mensajes con contexto de tracing
    public <K, V> ProducerRecord<K, V> injectTracingContext(ProducerRecord<K, V> record) {
        Span currentSpan = Span.current();
        if (!currentSpan.getSpanContext().isValid()) {
            return record;
        }

        Span span = tracer.spanBuilder("send-to-" + record.topic())
                .setSpanKind(SpanKind.PRODUCER)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Propagar contexto a los headers
            propagator.inject(Context.current(), record, (carrier, key, value) ->
                    carrier.headers().add(new RecordHeader(key, value.getBytes())));

            span.setAttribute("messaging.system", "kafka");
            span.setAttribute("messaging.destination", record.topic());
            if (record.key() != null) {
                span.setAttribute("messaging.key", record.key().toString());
            }
        } finally {
            span.end();
        }

        return record;
    }

    // Para consumir mensajes y extraer contexto de tracing
    public <T> Mono<T> tracedOperation(ConsumerRecord<?, ?> record, Mono<T> operation) {
        TextMapGetter<ConsumerRecord<?,?>> getter = new TextMapGetter<ConsumerRecord<?, ?>>() {
            @Override
            public Iterable<String> keys(ConsumerRecord<?, ?> carrier) {
                List<String> keys = new ArrayList<>();
                carrier.headers().forEach(header -> keys.add(header.key()));
                return keys;
            }

            @Override
            public String get(@Nullable ConsumerRecord<?, ?> carrier, String key) {
                Header header = carrier.headers().lastHeader(key);
                if(header!=null){
                    return new String(header.value());
                }
                return null;
            }
        };

        Context extractedContext = propagator.extract(Context.current(), record, getter);

        Span span = tracer.spanBuilder("process-from-" + record.topic())
            .setSpanKind(SpanKind.CONSUMER)
            .setParent(extractedContext)
            .startSpan();

        span.setAttribute("messaging.system", "kafka");
        span.setAttribute("messaging.destination", record.topic());
        span.setAttribute("messaging.consumer_group", "module-almacen-group");

        return Mono.using(
                () -> span.makeCurrent(),
                scope -> {
                    // Configurar MDC para logging
                    MDC.put("traceId", span.getSpanContext().getTraceId());
                    MDC.put("spanId", span.getSpanContext().getSpanId());
                    MDC.put("topic", record.topic());

                    return operation
                            .doOnError(e -> span.recordException(e))
                            .doFinally(signal -> {
                                MDC.remove("traceId");
                                MDC.remove("spanId");
                                MDC.remove("topic");
                            });
                },
                scope -> span.end()
        );
    }
}
