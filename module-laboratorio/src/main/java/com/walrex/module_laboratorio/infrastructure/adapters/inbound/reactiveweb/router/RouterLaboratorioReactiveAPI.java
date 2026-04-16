package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler.EtapaTinturaHandler;
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
public class RouterLaboratorioReactiveAPI {
    private final EtapaTinturaHandler etapaTinturaHandler;
    private static final String PATH_LABORATORIO = "laboratorio";

    @Bean
    public RouterFunction<ServerResponse> etapaTinturaRoutes() {
        return RouterFunctions.route()
                .path("/"+ PATH_LABORATORIO, builder -> builder
                        .POST("/etapa-tintura", accept(APPLICATION_JSON), etapaTinturaHandler::create)
                        .GET("/etapa-tintura", accept(APPLICATION_JSON), etapaTinturaHandler::findAll)
                        .GET("/etapa-tintura/{id}", accept(APPLICATION_JSON), etapaTinturaHandler::findById)
                        .PUT("/etapa-tintura/{id}", accept(APPLICATION_JSON), etapaTinturaHandler::update)
                        .DELETE("/etapa-tintura/{id}", accept(APPLICATION_JSON), etapaTinturaHandler::delete)
                )
                .before(request -> {
                    log.info("🔄 Router {} recibió solicitud: {} {}", PATH_LABORATORIO,
                        request.method(), request.path());
                    return request;
                })
                .after((request, response) -> {
                    log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_LABORATORIO+"/etapa-tintura",
                        request.method(),
                        request.path(), response.statusCode());
                    return response;
                })
                .build();
    }

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de laboratorio registradas en: /{}/etapa-tintura", PATH_LABORATORIO);
    }
}
