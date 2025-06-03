package com.walrex.module_articulos.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_articulos.infrastructure.adapters.inbound.reactiveweb.ArticuloHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterProductReactiveAPI {
    private final ArticuloHandler articuloHandler;
    private static final String PATH_PRODUCTS="products";

    @Bean
    public RouterFunction<ServerResponse> productsRouter(){
        return RouterFunctions.route()
            .path("/"+PATH_PRODUCTS, builder -> builder
                .GET("/search", articuloHandler::searchArticulos)
            )
            .before(request -> {
                log.info("🔄 Router recibió solicitud: {} {}", request.method(), request.path());
                return request;
            })
            .after((request, response) -> {
                log.info("✅ Router respondió a: {} {} con estado: {}", request.method(), request.path(), response.statusCode());
                return response;
            })
            .build();
    }
    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de artículos registradas en: /{}", PATH_PRODUCTS);
    }

}
