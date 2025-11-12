package com.walrex.gateway.gateway.infrastructure.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.walrex.gateway.gateway.config.DynamicModuleRouteFilter;
import com.walrex.gateway.gateway.infrastructure.config.filter.JwtHeaderFilter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DynamicRouteConfig {
    private final DynamicModuleRouteFilter dynamicModuleRouteFilter;
    private final JwtHeaderFilter jwtHeaderFilter;

    @Bean
    public JwtHeaderFilter.Config jwtHeaderFilterConfig() {
        return new JwtHeaderFilter.Config();
    }

    @Bean
    public RouteLocator dynamicRouteLocator(RouteLocatorBuilder builder) {
        log.info("🚀 [0] DynamicRouteConfig - INICIANDO configuración de rutas");
        return builder.routes()
                .route("dynamic-route-handler", r -> {
                    log.info("🔵 [0] DynamicRouteConfig - Configurando ruta dinámica global");
                    return r
                            .path("/**")
                            .filters(f -> {
                                log.info("🔵 [0] DynamicRouteConfig - Aplicando filtros Gateway");
                                log.info("🔵 [0] DynamicRouteConfig - JwtHeaderFilter configurado: {}",
                                        jwtHeaderFilter != null);
                                return f
                                        .filter(jwtHeaderFilter.apply(jwtHeaderFilterConfig())) // ✅ JWT con
                                                                                                // configuración
                                                                                                // correcta
                                        .filter(dynamicModuleRouteFilter.apply(new DynamicModuleRouteFilter.Config())); // ✅
                                                                                                                        // Routing
                                                                                                                        // después
                            })
                            .uri("forward:/"); // URI placeholder, la verdadera URI se determinará en el filtro
                }).build();
    }

    @PostConstruct
    public void init() {
        log.info("Bean DynamicRouteConfig inicializado correctamente first");
    }
}