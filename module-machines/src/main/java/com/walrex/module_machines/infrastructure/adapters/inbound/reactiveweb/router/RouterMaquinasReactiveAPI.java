package com.walrex.module_machines.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_machines.infrastructure.adapters.inbound.reactiveweb.handler.MaquinaHandler;
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
public class RouterMaquinasReactiveAPI {

    private final MaquinaHandler maquinaHandler;
    private static final String PATH_BASE = "/machines";

    @PostConstruct
    public void init() {
        log.info("Rutas del módulo de máquinas registradas en: {}", PATH_BASE);
    }

    @Bean
    public RouterFunction<ServerResponse> maquinaRoutes() {
        return RouterFunctions.route()
                .GET(PATH_BASE, accept(APPLICATION_JSON), maquinaHandler::findAll)
                .build();
    }
}
