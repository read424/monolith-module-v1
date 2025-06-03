package com.walrex.user.module_users.common;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LoggingContextUtil {

    // Operador para capturar y propagar el contexto MDC
    public <T> Mono<T> withLoggingContext(Mono<T> mono, Map<String, String> contextMap) {
        return mono
                .doOnSubscribe(s -> contextMap.forEach(MDC::put))
                .doFinally(signal -> contextMap.keySet().forEach(MDC::remove));
    }

    // Capturar el contexto MDC actual para propiedades específicas
    public Map<String, String> captureContextMap(String... keys) {
        Map<String, String> contextMap = new HashMap<>();
        for (String key : keys) {
            String value = MDC.get(key);
            if (value != null) {
                contextMap.put(key, value);
            }
        }
        return contextMap;
    }

    // Método de utilidad para propagar el contexto de trazado
    public <T> Mono<T> withTraceContext(Mono<T> mono) {
        Map<String, String> contextMap = captureContextMap("traceId", "spanId", "correlationId");
        return withLoggingContext(mono, contextMap);
    }
}
