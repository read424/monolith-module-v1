package com.walrex.user.module_users.infrastructure.adapters.security.jwt.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtFilter implements WebFilter {

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

        if(path.contains("auth") || path.contains("graphiql") || path.contains("graphql"))
            return chain.filter(exchange);

        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(auth == null)
            return Mono.error(new Throwable("no token was found"));
        if(!auth.startsWith("Bearer "))
            return Mono.error(new Throwable("invalid auth"));

        String token = auth.replace("Bearer ", "");
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange);
    }
}
