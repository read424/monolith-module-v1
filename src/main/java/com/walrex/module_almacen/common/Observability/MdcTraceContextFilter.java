package com.walrex.module_almacen.common.Observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MdcTraceContextFilter {
    private final OpenTelemetry openTelemetry;

    @PostConstruct
    public void init() {
        // En versiones más recientes de OpenTelemetry, no se usa registerHook
        // En su lugar, puedes crear un SpanProcessor personalizado

        SpanProcessor mdcSpanProcessor = new SpanProcessor() {
            @Override
            public void onStart(Context parentContext, ReadWriteSpan span) {
                if (span.getSpanContext().isValid()) {
                    MDC.put("traceId", span.getSpanContext().getTraceId());
                    MDC.put("spanId", span.getSpanContext().getSpanId());
                }
            }

            @Override
            public boolean isStartRequired() {
                return true;
            }

            @Override
            public void onEnd(ReadableSpan span) {
                // Opcional: limpiar MDC al finalizar el span
                // MDC.remove("traceId");
                // MDC.remove("spanId");
            }

            @Override
            public boolean isEndRequired() {
                return false;
            }
        };
    }
}
