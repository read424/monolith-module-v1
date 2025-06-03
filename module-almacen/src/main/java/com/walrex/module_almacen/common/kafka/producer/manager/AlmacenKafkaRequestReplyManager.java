package com.walrex.module_almacen.common.kafka.producer.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlmacenKafkaRequestReplyManager {
    private final Map<String, Sinks.One<String>> pendingRequest = new ConcurrentHashMap<>();

    public Mono<String> registerRequest(String correlationId){
        Sinks.One<String> sink = Sinks.one();
        pendingRequest.put(correlationId, sink);

        // Limpieza automática después de un tiempo para evitar memory leaks
        return sink.asMono()
                .timeout(Duration.ofMinutes(5))
                .doOnError(TimeoutException.class, ex-> cleanUpRequest(correlationId, "🧹 Solicitud expirada"));
    }

    public void completeRequest(String correlationId, String response){
        Sinks.One<String> sink = pendingRequest.remove(correlationId);
        if(sink!=null){
            sink.tryEmitValue(response);
        }
    }

    /**
     * Elimina una solicitud pendiente del mapa sin completarla ni emitir error.
     * Útil para limpieza en caso de errores externos.
     *
     * @param correlationId El ID de correlación de la solicitud a eliminar
     * @return true si la solicitud existía y fue eliminada, false en caso contrario
     */
    public boolean removeRequest(String correlationId) {
        Sinks.One<String> removed = pendingRequest.remove(correlationId);
        if (removed != null) {
            log.debug("🗑️ Solicitud con correlationId {} removida del mapa", correlationId);
            return true;
        }
        return false;
    }

    /**
     * Elimina una solicitud pendiente y emite un error al cliente que está esperando.
     *
     * @param correlationId El ID de correlación de la solicitud a eliminar
     * @param errorMessage El mensaje de error a emitir
     * @return true si la solicitud existía y fue eliminada, false en caso contrario
     */
    public boolean removeRequestWithError(String correlationId, String errorMessage) {
        Sinks.One<String> sink = pendingRequest.remove(correlationId);
        if(sink != null) {
            sink.tryEmitError(new RuntimeException(errorMessage));
            log.debug("🗑️ Solicitud con correlationId {} removida con error: {}", correlationId, errorMessage);
            return true;
        }
        return false;
    }

    private void cleanUpRequest(String correlationId, String message) {
        emitResponseOrError(correlationId, null, new TimeoutException(message));
    }
    private void emitResponseOrError(String correlationId, String response, Throwable error) {
        Sinks.One<String> sink = pendingRequest.remove(correlationId);
        if (sink != null) {
            if (error != null) {
                sink.tryEmitError(error);
            } else {
                sink.tryEmitValue(response);
            }
        }
    }
}
