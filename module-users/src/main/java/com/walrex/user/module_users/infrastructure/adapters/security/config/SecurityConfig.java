package com.walrex.user.module_users.infrastructure.adapters.security.config;

import com.walrex.user.module_users.infrastructure.adapters.security.jwt.filter.JwtFilter;
import com.walrex.user.module_users.infrastructure.adapters.security.repository.SecurityContextRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@Order(-1)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private static final String[] WHITE_LIST_URL = {
        "api/v2/auth/**",
        "api/v2/document/**"
    };
    private final SecurityContextRepository securityContextRepository;

    @PostConstruct
    public void init() {
        log.error("🟡 [USERS-SECURITY] Configuración inicializada con Order: {}", 
            this.getClass().getAnnotation(Order.class) != null ? 
            this.getClass().getAnnotation(Order.class).value() : "Sin Order");
        log.info("Configuración de seguridad inicializada - Todas las rutas permitidas");
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchangeSpec -> exchangeSpec
                        .pathMatchers("/document/**", "/api/v2/auth/**", "/auth/**").permitAll()
                        .pathMatchers("/graphql", "/graphql/**", "/graphiql", "/graphiql/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAfter(jwtFilter, SecurityWebFiltersOrder.FIRST)
                .securityContextRepository(securityContextRepository)
                //.addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .build();
    }

    //@Bean
    //public ServerSecurityContextRepository securityContextRepository() {
    //    return new ServerSecurityContextRepository() {
    //        @Override
    //        public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
    //            return Mono.empty(); // No es necesario guardar el contexto en este caso
    //        }

    //        @Override
    //        public Mono<SecurityContext> load(ServerWebExchange exchange) {
    //            return Mono.justOrEmpty(exchange.getAttribute(SecurityContext.class.getName()));
    //        }
    //    };
    //}

    //@Bean
    //public SecurityContextServerWebExchangeWebFilter securityContextWebFilter() {
    //    return new SecurityContextServerWebExchangeWebFilter(securityContextRepository());
    //}
}
