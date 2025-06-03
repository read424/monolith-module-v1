package com.walrex.gateway.gateway.infrastructure.config;

import com.walrex.gateway.gateway.config.DynamicModuleRouteFilter;
import com.walrex.gateway.gateway.infrastructure.config.filter.JwtHeaderFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
@Order(-100)
@Slf4j
public class DynamicRouteConfig {
    private final DynamicModuleRouteFilter dynamicModuleRouteFilter;
    private final JwtHeaderFilter jwtHeaderFilter;

    @Bean
    public RouteLocator dynamicRouteLocator(RouteLocatorBuilder builder){
        log.error("🚀 [0] DynamicRouteConfig - INICIANDO configuración de rutas (Order -100)");
        return builder.routes()
                .route("dynamic-route-handler", r -> {
                    log.error("🔵 [0] DynamicRouteConfig - Configurando ruta dinámica global");
                    return r
                            .path("/**")
                            .filters(f -> {
                                log.error("🔵 [0] DynamicRouteConfig - Aplicando filtros Gateway");
                                return f
                                        .filter(jwtHeaderFilter.apply(new JwtHeaderFilter.Config()))
                                        .filter(dynamicModuleRouteFilter.apply(new DynamicModuleRouteFilter.Config()));
                            }) // Inyectaremos el repositorio mediante constructor
                            .uri("forward:/"); // URI placeholder, la verdadera URI se determinará en el filtro
                }).build();
    }

    @PostConstruct
    public void init() {
        log.info("Bean DynamicRouteConfig inicializado correctamente first");
    }
}