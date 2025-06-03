package com.walrex.module_core.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//@Configuration
//@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

    //@PostConstruct
    //public void init() {
    //    log.info("Configuración de seguridad inicializada - Todas las rutas permitidas");
    //}

    //@Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
        log.info("Configurando SecurityWebFilterChain con acceso permitido a todas las rutas");
        return http
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            )
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .cors(ServerHttpSecurity.CorsSpec::disable)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }
}