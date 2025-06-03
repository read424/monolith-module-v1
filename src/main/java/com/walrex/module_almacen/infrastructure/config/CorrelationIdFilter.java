package com.walrex.module_almacen.infrastructure.config;

import com.walrex.module_almacen.common.utils.CorrelationIdUtils;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Configuration
public class CorrelationIdFilter implements WebFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = CorrelationIdUtils.generateCorrelationId();
        }

        MDC.put(CorrelationIdUtils.CORRELATION_ID, correlationId);
        exchange.getResponse().getHeaders().add(CORRELATION_HEADER, correlationId);

        final String finalCorrelationId = correlationId;
        return chain.filter(exchange)
                .contextWrite(Context.of(CorrelationIdUtils.CORRELATION_ID, finalCorrelationId))
                .doFinally(signalType -> MDC.remove(CorrelationIdUtils.CORRELATION_ID));
    }
}
