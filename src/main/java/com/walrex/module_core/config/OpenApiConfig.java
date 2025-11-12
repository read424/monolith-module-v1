package com.walrex.module_core.config;

import java.util.List;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuración de OpenAPI para el module-core
 * 
 * Proporciona documentación automática de las APIs REST
 * accesible en /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

        @Bean("moduleCoreOpenApiConfig")
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Module Core API")
                                                .description("API del módulo core del sistema monolito modular")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Walrex Development Team")
                                                                .email("desarrollo@walrex.com")
                                                                .url("https://walrex.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8088")
                                                                .description("Servidor de desarrollo"),
                                                new Server()
                                                                .url("https://api.walrex.com")
                                                                .description("Servidor de producción")));
        }

        /**
         * Configuración específica para el módulo de almacén
         */
        @Bean
        public GroupedOpenApi motivosDevolucionApi() {
                return GroupedOpenApi.builder()
                                .group("motivos-devolucion")
                                .displayName("Motivos de Devolución")
                                .pathsToMatch("/almacen/motivos-devolucion/**")
                                .build();
        }
}