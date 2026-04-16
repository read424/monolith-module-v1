package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler.CurvaDisenoHandler;
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
public class RouterCurvaDisenoReactiveAPI {

    private static final String PATH_BASE = "/laboratorio/curva-diseno";

    private final CurvaDisenoHandler curvaDisenoHandler;

    @PostConstruct
    public void init() {
        log.info("Rutas de curva de diseño registradas en: {}", PATH_BASE);
    }

    @Bean
    public RouterFunction<ServerResponse> curvaDisenoRoutes() {
        return RouterFunctions.route()
                .POST(PATH_BASE, accept(APPLICATION_JSON), curvaDisenoHandler::create)
                .GET(PATH_BASE, accept(APPLICATION_JSON), curvaDisenoHandler::findAll)
                .GET(PATH_BASE + "/{id}/pdf", curvaDisenoHandler::generatePdf)
                .GET(PATH_BASE + "/{id}", accept(APPLICATION_JSON), curvaDisenoHandler::findById)
                .build();
    }
}
