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

/**
 * Configuración del pipeline de filtros del Gateway
 *
 * Pipeline activo:
 * 1. JwtHeaderFilter - Validación JWT y contexto de usuario
 * 2. DynamicModuleRouteFilter - Resolución dinámica de rutas y routing
 */
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

    /**
     * Configura el RouteLocator con el pipeline de filtros
     */
    @Bean
    public RouteLocator dynamicRouteLocator(RouteLocatorBuilder builder) {
        log.info("Iniciando configuración de rutas dinámicas");

        return builder.routes()
            .route("dynamic-route-handler", r -> r
                .path("/**")
                .filters(f -> f
                    .filter(jwtHeaderFilter.apply(jwtHeaderFilterConfig()))
                    .filter(dynamicModuleRouteFilter.apply(new DynamicModuleRouteFilter.Config())))
                .uri("forward:/"))
            .build();
    }

    @PostConstruct
    public void init() {
        log.info("DynamicRouteConfig inicializado - Pipeline: JwtHeaderFilter -> DynamicModuleRouteFilter");
    }
}