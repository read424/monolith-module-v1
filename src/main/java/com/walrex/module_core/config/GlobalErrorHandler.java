package com.walrex.module_core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@Slf4j
public class GlobalErrorHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.debug("GlobalErrorHandler manejando excepción: {}", ex.getClass().getSimpleName());

        if (ex instanceof ServerWebInputException) {
            return handleValidationError(exchange, (ServerWebInputException) ex);
        }

        if (ex instanceof ResponseStatusException) {
            return handleResponseStatusError(exchange, (ResponseStatusException) ex);
        }
        return Mono.error(ex); // Dejar que otros handlers lo manejen
    }

    private Mono<Void> handleValidationError(ServerWebExchange exchange, ServerWebInputException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Validation Error\",\"message\":\"%s\"}", ex.getMessage());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleResponseStatusError(ServerWebExchange exchange, ResponseStatusException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(ex.getStatusCode());
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}",
                ex.getStatusCode(),
                ex.getReason()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
