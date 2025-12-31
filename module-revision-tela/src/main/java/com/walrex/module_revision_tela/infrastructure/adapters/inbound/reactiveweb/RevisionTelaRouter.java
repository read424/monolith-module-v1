package com.walrex.module_revision_tela.infrastructure.adapters.inbound.reactiveweb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Router de WebFlux para los endpoints del módulo de revisión de tela
 * Path base: /revision_tela
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Configuration
public class RevisionTelaRouter {

    /**
     * Define las rutas para el recurso fixed-status-guie
     *
     * @param handler Handler con la lógica del endpoint
     * @return RouterFunction configurada
     */
    @Bean
    public RouterFunction<ServerResponse> fixedStatusGuieRoutes(FixedStatusGuieHandler handler) {
        return RouterFunctions.route()
            .POST("/revision_tela/fixed-status-guie", handler::ejecutarCorreccionStatus)
            .build();
    }
}
