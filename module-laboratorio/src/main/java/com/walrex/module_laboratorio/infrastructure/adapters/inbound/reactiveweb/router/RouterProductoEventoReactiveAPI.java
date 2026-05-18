package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler.ProductoEventoHandler;
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
public class RouterProductoEventoReactiveAPI {

    private static final String PATH_PRODUCT_EVENT = "/laboratorio/producto-evento";

    private final ProductoEventoHandler productoEventoHandler;

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas de producto evento registradas en: {}", PATH_PRODUCT_EVENT);
    }

    @Bean
    public RouterFunction<ServerResponse> productoEventoRoutes() {
        return RouterFunctions.route()
                .POST(PATH_PRODUCT_EVENT, accept(APPLICATION_JSON), productoEventoHandler::create)
                .GET(PATH_PRODUCT_EVENT, accept(APPLICATION_JSON), productoEventoHandler::findAll)
                .GET(PATH_PRODUCT_EVENT + "/{id}", accept(APPLICATION_JSON), productoEventoHandler::findById)
                .PUT(PATH_PRODUCT_EVENT + "/{id}", accept(APPLICATION_JSON), productoEventoHandler::update)
                .DELETE(PATH_PRODUCT_EVENT + "/{id}", accept(APPLICATION_JSON), productoEventoHandler::delete)
            .before(request -> {
                log.info("🔄 Router {} recibió solicitud: {} {}", PATH_PRODUCT_EVENT,
                    request.method(), request.path());
                return request;
            })
            .after((request, response) -> {
                log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_PRODUCT_EVENT+"/etapa-tintura",
                    request.method(),
                    request.path(), response.statusCode());
                return response;
            })
            .build();
    }
}
