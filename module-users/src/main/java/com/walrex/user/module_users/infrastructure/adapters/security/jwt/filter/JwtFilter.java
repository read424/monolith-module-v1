package com.walrex.user.module_users.infrastructure.adapters.security.jwt.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtFilter implements WebFilter {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "auth",
        "graphiql",
        "graphql",
        "products/search",
        "almacen/pesaje",
        "almacen/guide-pending",
        "almacen/session-articulo-pesaje",
        "almacen/guide-no-rolls"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        ServerHttpRequest request = exchange.getRequest();
        log.error("🔵 [USERS-JWT] Procesando: {} {} - Thread: {}",
            request.getMethod(),
            request.getPath().value(),
            Thread.currentThread().getName());
        String path = request.getPath().value();

        // ✅ SALTEAR JWT para requests OPTIONS (preflight)
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.debug("Saltando validación JWT para OPTIONS request: {}", path);
            return chain.filter(exchange);
        }

        log.info("🔍 [USERS-JWT] path request: '{}'", path);
        log.info("🔍 [USERS-JWT] List of exclusions: {}", EXCLUDED_PATHS);

        boolean isExcluded = EXCLUDED_PATHS.stream().anyMatch(path::contains);
        log.info("🔍 [USERS-JWT] ¿Is path excluded?: {}", isExcluded);

        if (isExcluded) {
            log.info("🟢 [USERS-JWT] PATH EXCLUIDO: {}", path);
            return chain.filter(exchange);
        }

        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(auth == null)
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no token was found"));
        if(!auth.startsWith("Bearer "))
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid auth"));

        String token = auth.replace("Bearer ", "");
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange);
    }
}
