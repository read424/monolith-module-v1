package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler.GamaHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterGamaReactiveAPI {

    private final GamaHandler gamaHandler;
    private static final String PATH_LABORATORIO = "laboratorio";

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de laboratorio registradas en: /{}/gamas", PATH_LABORATORIO);
    }

    @Bean
    public RouterFunction<ServerResponse> gamaRoutes() {
        return RouterFunctions.route()
            .path("/"+PATH_LABORATORIO, builder -> builder
                .POST("/gamas", accept(APPLICATION_JSON), gamaHandler::create)
                .GET("/gamas", accept(APPLICATION_JSON), gamaHandler::findAll)
                .GET("/gamas/active", accept(APPLICATION_JSON), gamaHandler::findActive)
                .GET("/gamas/{id}", accept(APPLICATION_JSON), gamaHandler::findById)
                .PUT("/gamas/{id}", accept(APPLICATION_JSON), gamaHandler::update)
                .DELETE("/gamas/{id}", accept(APPLICATION_JSON), gamaHandler::delete)
            )
            .before(request -> {
                log.info("🔄 Router {} recibió solicitud: {} {}", PATH_LABORATORIO+"/gamas",
                    request.method(), request.path());
                return request;
            })
            .after((request, response) -> {
                log.error("✅ [GAMA-ROUTER] respondió a: {} {} con estado: {}",
                    request.method(), request.path(), response.statusCode());
                return response;
            })
            .build();
    }
}
