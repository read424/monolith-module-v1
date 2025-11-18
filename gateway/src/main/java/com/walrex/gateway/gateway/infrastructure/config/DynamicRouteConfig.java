package com.walrex.gateway.gateway.infrastructure.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.walrex.gateway.gateway.config.DynamicModuleRouteFilter;
import com.walrex.gateway.gateway.infrastructure.config.filter.JwtHeaderFilter;
import com.walrex.gateway.gateway.infrastructure.filters.RouteResolutionFilter;
import com.walrex.gateway.gateway.infrastructure.filters.RoutingStrategyFilter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración del pipeline de filtros del Gateway
 *
 * Pipeline actual (NUEVA ARQUITECTURA):
 * 1. JwtHeaderFilter - Validación JWT y contexto de usuario
 * 2. RouteResolutionFilter - Resolución de ruta desde BD/cache
 * 3. RoutingStrategyFilter - Delegación al handler apropiado
 *
 * DEPRECATED:
 * - DynamicModuleRouteFilter (mantenido para backward compatibility)
 *
 * Para activar la nueva arquitectura, descomentar los filtros nuevos
 * y comentar el DynamicModuleRouteFilter.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DynamicRouteConfig {

    // ========== NUEVA ARQUITECTURA (RECOMENDADO) ==========
    private final RouteResolutionFilter routeResolutionFilter;
    private final RoutingStrategyFilter routingStrategyFilter;

    // ========== ARQUITECTURA ANTIGUA (DEPRECATED) ==========
    private final DynamicModuleRouteFilter dynamicModuleRouteFilter;

    // ========== COMÚN ==========
    private final JwtHeaderFilter jwtHeaderFilter;

    @Bean
    public JwtHeaderFilter.Config jwtHeaderFilterConfig() {
        return new JwtHeaderFilter.Config();
    }

    @Bean
    public RouteResolutionFilter.Config routeResolutionFilterConfig() {
        return new RouteResolutionFilter.Config();
    }

    @Bean
    public RoutingStrategyFilter.Config routingStrategyFilterConfig() {
        return new RoutingStrategyFilter.Config();
    }

    /**
     * Configura el RouteLocator con el pipeline de filtros
     *
     * IMPORTANTE: Elegir entre nueva arquitectura o arquitectura antigua
     */
    @Bean
    public RouteLocator dynamicRouteLocator(RouteLocatorBuilder builder) {
        log.info("🚀 [DynamicRouteConfig] Iniciando configuración de rutas");

        return builder.routes()
            .route("dynamic-route-handler", r -> {
                log.info("🔵 [DynamicRouteConfig] Configurando ruta dinámica global");

                return r
                    .path("/**")
                    .filters(f -> {
                        log.info("🔵 [DynamicRouteConfig] Aplicando pipeline de filtros");

                        // ========================================
                        // ARQUITECTURA ANTIGUA (STABLE - FUNCIONANDO)
                        // ========================================
                        return f
                            .filter(jwtHeaderFilter.apply(jwtHeaderFilterConfig()))
                            .filter(dynamicModuleRouteFilter.apply(new DynamicModuleRouteFilter.Config()));

                        // ========================================
                        // NUEVA ARQUITECTURA (EN DESARROLLO - NO USAR AÚN)
                        // Descomentar cuando esté completamente validada
                        // ========================================
                        // return f
                        //     .filter(jwtHeaderFilter.apply(jwtHeaderFilterConfig()))
                        //     .filter(routeResolutionFilter.apply(routeResolutionFilterConfig()))
                        //     .filter(routingStrategyFilter.apply(routingStrategyFilterConfig()));
                    })
                    .uri("forward:/"); // Placeholder, URI real se determina en los filtros
            })
            .build();
    }

    @PostConstruct
    public void init() {
        log.info("✅ DynamicRouteConfig inicializado correctamente");
        log.info("📋 Pipeline activo: JwtHeaderFilter → DynamicModuleRouteFilter (STABLE)");
        log.info("ℹ️  Nueva arquitectura (RouteResolutionFilter + RoutingStrategyFilter) disponible pero NO ACTIVA");
    }
}