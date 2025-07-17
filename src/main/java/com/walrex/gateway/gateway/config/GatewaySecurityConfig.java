package com.walrex.gateway.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import com.walrex.gateway.gateway.infrastructure.config.filter.JwtHeaderFilter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Configuración de seguridad centralizada para el gateway
 * 
 * Actúa como perímetro de seguridad único para todo el sistema.
 * Valida JWT y agrega headers de contexto para módulos internos.
 */
@Configuration
@EnableWebFluxSecurity
@Slf4j
public class GatewaySecurityConfig {

    @PostConstruct
    public void init() {
        log.info("🔐 [GATEWAY-SECURITY] Configuración de seguridad centralizada inicializada");
        log.info("🎯 Gateway configurado como perímetro de seguridad único");
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, JwtHeaderFilter jwtHeaderFilter) {
        log.info("🔐 Configurando SecurityWebFilterChain para gateway");

        SecurityWebFilterChain filterChain = http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchangeSpec -> {
                    // ✅ PERMITIR TODAS LAS RUTAS - El JwtHeaderFilter manejará la autenticación
                    exchangeSpec.anyExchange().permitAll();
                    log.info(
                            "🔐 [GATEWAY-SECURITY] Configurado para permitir todas las rutas - JwtHeaderFilter manejará autenticación");
                })
                .securityContextRepository(gatewaySecurityContextRepository())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .build();

        log.info("🔐 [GATEWAY-SECURITY] SecurityWebFilterChain configurado con SecurityContextRepository");
        return filterChain;
    }

    /**
     * SecurityContextRepository para manejar el contexto de seguridad del gateway
     */
    @Bean
    public ServerSecurityContextRepository gatewaySecurityContextRepository() {
        return new ServerSecurityContextRepository() {
            @Override
            public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
                String path = exchange.getRequest().getPath().value();
                String threadName = Thread.currentThread().getName();
                log.info("🔵 [GATEWAY-SECURITY-CONTEXT] SAVE - Path: '{}', Thread: '{}', Context: {}",
                        path, threadName, context != null ? "PRESENTE" : "NULL");
                return Mono.empty(); // No es necesario guardar el contexto en este caso
            }

            @Override
            public Mono<SecurityContext> load(ServerWebExchange exchange) {
                String path = exchange.getRequest().getPath().value();
                String threadName = Thread.currentThread().getName();
                SecurityContext context = exchange.getAttribute(SecurityContext.class.getName());
                log.info("🔵 [GATEWAY-SECURITY-CONTEXT] LOAD - Path: '{}', Thread: '{}', Context: {}",
                        path, threadName, context != null ? "PRESENTE" : "NULL");
                return Mono.justOrEmpty(context);
            }
        };
    }
}