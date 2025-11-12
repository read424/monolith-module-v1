package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.OrdenProduccionHandler;
import com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.SolicitudCambioHandler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrdenProduccionRouterReactiveAPI {

    private static final String PATH_COMERCIAL = "comercial";
    private final OrdenProduccionHandler ordenProduccionHandler;
    private final SolicitudCambioHandler solicitudCambioHandler;

    @Bean
    public RouterFunction<ServerResponse> ordenProduccionRoutes() {
        return RouterFunctions.route()
                .path("/" + PATH_COMERCIAL, builder -> builder
                        .GET("/orden-produccion-partida/{idPartida}",
                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                ordenProduccionHandler::getOrdenProduccionPorPartida)
                        .POST("/guardar-solicitud-cambio-servicio",
                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                solicitudCambioHandler::guardarSolicitudCambioServicio))
                .before(request -> {
                    log.info("🔄 Router {} recibió solicitud: {} {}",
                            PATH_COMERCIAL,
                            request.method(),
                            request.path());
                    return request;
                })
                .after((request, response) -> {
                    log.info("✅ Router {} respondió a: {} {} con estado: {}",
                            PATH_COMERCIAL,
                            request.method(),
                            request.path(),
                            response.statusCode());
                    return response;
                })
                .build();
    }

    @PostConstruct
    public void init() {
        System.out.println("🔌 Rutas del módulo comercial registradas en: /" + PATH_COMERCIAL);
        log.info("🔌 Rutas del módulo comercial registradas en: /{}", PATH_COMERCIAL);
    }
}