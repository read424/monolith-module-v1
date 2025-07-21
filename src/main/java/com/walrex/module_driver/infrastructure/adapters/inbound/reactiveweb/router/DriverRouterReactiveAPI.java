package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.DriverHandler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DriverRouterReactiveAPI {
    private static final String PATH_DRIVER = "driver";
    private final DriverHandler driverHandler;

    @Bean
    public RouterFunction<ServerResponse> driverRoutes() {
        return RouterFunctions.route()
                .path("/" + PATH_DRIVER, builder -> builder
                        .POST("/", RequestPredicates.accept(MediaType.APPLICATION_JSON), driverHandler::createDriver)
                        .PUT("/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), driverHandler::updateDriver)
                        .DELETE("/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                driverHandler::deleteDriver)
                        .GET("/{id}", RequestPredicates.accept(MediaType.APPLICATION_JSON), driverHandler::getDriver))
                .before(request -> {
                    log.info("🔄 Router {} recibió solicitud: {} {}", PATH_DRIVER,
                            request.method(), request.path());
                    return request;
                }).after((request, response) -> {
                    log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_DRIVER,
                            request.method(), request.path(), response.statusCode());
                    return response;
                })
                .build();
    }

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de driver registradas en: /{}", PATH_DRIVER);
    }
}
