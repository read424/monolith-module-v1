package com.walrex.gateway.gateway.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity.ModulesUrl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProxyRequestBuilder {

    private final PathTransformer pathTransformer;

    public Mono<Void> buildExternalProxy(ModulesUrl module, String requestPath,
                                         String serviceUri, ServerWebExchange exchange,
                                         GatewayFilterChain chain) {
        String newPath = pathTransformer.stripPrefix(requestPath, module);
        String fullUrl = serviceUri + newPath;

        String queryString = exchange.getRequest().getURI().getRawQuery();
        if (queryString != null && !queryString.isEmpty()) {
            fullUrl += "?" + queryString;
        }

        log.info("Proxy externo: '{}' → '{}'", requestPath, fullUrl);

        try {
            URI externalUri = new URI(fullUrl);
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, externalUri);

            final String finalUrl = fullUrl;
            return chain.filter(exchange)
                .doOnSuccess(v -> log.info("Proxy externo completado: {}", finalUrl))
                .doOnError(e -> log.error("Error en proxy externo '{}': {}", finalUrl, e.getMessage()));

        } catch (URISyntaxException e) {
            log.error("URI inválida: {}", fullUrl, e);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            String errorBody = String.format(
                "{\"error\":\"Bad Gateway\",\"message\":\"URI inválida: %s\",\"path\":\"%s\"}",
                fullUrl, requestPath);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorBody.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}
