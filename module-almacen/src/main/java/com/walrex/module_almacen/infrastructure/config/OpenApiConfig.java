package com.walrex.module_almacen.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración OpenAPI/Swagger para el módulo de almacén
 * Documentación específica para endpoints de motivos de devolución
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI almacenOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Módulo Almacén - API")
                        .description("API para gestión de motivos de devolución en el módulo de almacén")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo")
                ));
    }

    @Bean
    public GroupedOpenApi motivosDevolucionApi() {
        return GroupedOpenApi.builder()
                .group("motivos-devolucion")
                .displayName("Motivos de Devolución")
                .pathsToMatch("/almacen/motivos-devolucion/**")
                .build();
    }
}
