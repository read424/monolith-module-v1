package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler.RecetaHandler;
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
public class RouterRecetasReactiveAPI {

    private final RecetaHandler recetaHandler;
    private static final String PATH_BASE = "/laboratorio/receta";

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas de recetas registradas en: {}", PATH_BASE);
    }

    @Bean
    public RouterFunction<ServerResponse> recetaRoutes() {
        return RouterFunctions.route()
                .GET(PATH_BASE, accept(APPLICATION_JSON), recetaHandler::findAll)
                .GET(PATH_BASE + "/{id}/curva-diseno", accept(APPLICATION_JSON), recetaHandler::getCurvaDisenoById)
                .GET(PATH_BASE + "/{id}/curva-diseno/pdf", accept(APPLICATION_JSON), recetaHandler::generateCurvaDisenoPdf)
                .PUT(PATH_BASE + "/{id}/curva-diseno", accept(APPLICATION_JSON), recetaHandler::updateCurvaDiseno)
                .build();
    }
}
