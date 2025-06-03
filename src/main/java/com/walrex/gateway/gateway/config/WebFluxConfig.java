package com.walrex.gateway.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;

import java.net.URI;

@Configuration
@Slf4j
public class WebFluxConfig {

    @Bean
    @Order(-2)
    public WebFilter logRequestFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String originalPath = request.getPath().value();
            String fullUri = request.getURI().toString();
            String threadName = Thread.currentThread().getName();

            log.error("🟢 [1] WebFlux-logRequestFilter [{}] - Path: '{}', URI: '{}'",
                    threadName, originalPath, fullUri);

            log.info("⚠️ Filtro WebFlux - Solicitud entrante: {} {}", request.getMethod(), originalPath);

            // Guardar la ruta original en un atributo del exchange para usarla después
            exchange.getAttributes().put("ORIGINAL_PATH", originalPath);

            return chain.filter(exchange)
                .doOnSuccess(v -> log.error("🟢 [1] WebFlux-logRequestFilter ÉXITO - Path: '{}'", originalPath))
                .doOnError(e -> log.error("🔴 [1] WebFlux-logRequestFilter ERROR - Path: '{}', Error: {}",
                        originalPath, e.getMessage()));
        };
    }

    /**
     * Este bean asegura que las rutas WebFlux tengan prioridad sobre el Gateway
     * para las rutas que deben manejarse internamente
     */
    @Bean
    @Order(-1) // Prioridad alta (se ejecuta antes)
    public WebFilter forwardedHeaderFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            URI uri = request.getURI();
            String path = request.getPath().value();
            String threadName = Thread.currentThread().getName();
            // Para debugging: imprime la URI original para ver qué está llegando
            log.error("🟡 [2] WebFlux-forwardedHeaderFilter [{}] - Path: '{}', URI: '{}'",
                    threadName, path, uri);
            // Si la URI original indica una redirección interna, la manejamos aquí
            if (uri.toString().startsWith("forward:")) {
                String forwardPath = uri.toString().substring("forward:".length());
                log.error("🔄 [2] FORWARD DETECTADO - De '{}' a '{}'", path, forwardPath);
                ServerHttpRequest modifiedRequest = request.mutate()
                    .path(forwardPath)
                    .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
            log.error("🟡 [2] WebFlux-forwardedHeaderFilter - NO ES FORWARD, continuando");
            return chain.filter(exchange);
        };
    }
}
