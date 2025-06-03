package com.walrex.module_almacen.common.utils;

import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;
import java.util.function.Function;

public class CorrelationIdUtils {
    public static final String CORRELATION_ID = "correlationId";

    /**
     * Genera un nuevo ID de correlación si no existe
     * @return ID de correlación
     */
    public static String getOrCreateCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID);
        if (correlationId == null || correlationId.isEmpty() || correlationId.equals("NONE")) {
            correlationId = generateCorrelationId();
            MDC.put(CORRELATION_ID, correlationId);
        }
        return correlationId;
    }

    /**
     * Genera un nuevo ID de correlación
     * @return Nuevo ID de correlación
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Establece el ID de correlación en el MDC y lo añade al contexto de Reactor
     * @param correlationId ID de correlación a establecer
     * @return Contexto de Reactor con el ID de correlación
     */
    public static Context withCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId);
        return Context.of(CORRELATION_ID, correlationId);
    }

    /**
     * Método para transformar un Mono añadiendo contexto de correlación
     * @param mono Mono a transformar
     * @param correlationId ID de correlación
     * @param <T> Tipo de dato del Mono
     * @return Mono con contexto de correlación
     */
    public static <T> Mono<T> withCorrelationId(Mono<T> mono, String correlationId) {
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }
        final String finalCorrelationId = correlationId;
        return mono.contextWrite(ctx -> withCorrelationId(finalCorrelationId));
    }

    /**
     * Método para crear un operador que extrae el ID de correlación del contexto y lo establece en el MDC
     * @param <T> Tipo de dato del Mono
     * @return Función que transforma el Mono
     */
    public static <T> Function<Mono<T>, Mono<T>> withContextualLogs() {
        return mono -> mono.doOnEach(signal -> {
            signal.getContextView().getOrEmpty(CORRELATION_ID)
                    .ifPresent(cid -> MDC.put(CORRELATION_ID, cid.toString()));
        }).doFinally(signalType -> MDC.remove(CORRELATION_ID));
    }
}
