package com.walrex.module_revision_tela.infrastructure.adapters.inbound.reactiveweb;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
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
@RequiredArgsConstructor
@Slf4j
public class RevisionTelaRouter {
    private final FixedStatusGuieHandler fixedStatusGuieHandler;
    private final ProcesarRevisionInventarioHandler procesarRevisionInventarioHandler;
    private final AnalysisInventoryLiftingHandler analysisInventoryLiftingHandler;
    private final DisableRollsHandler disableRollsHandler;
    private static final String PATH_REVISION = "revision_tela";

    @Bean
    public RouterFunction<ServerResponse> revisionRouter() {
        return RouterFunctions.route()
            .path("/" + PATH_REVISION, builder -> builder
                .POST("/fixed-status-guie",
                    RequestPredicates.accept(MediaType.APPLICATION_JSON),
                    fixedStatusGuieHandler::ejecutarCorreccionStatus
                )
                .POST("/procesar-revision-inventario",
                    RequestPredicates.accept(MediaType.APPLICATION_JSON),
                    procesarRevisionInventarioHandler::procesarRevision
                )
                .POST("/execute-analysis-lifting",
                    RequestPredicates.accept(MediaType.APPLICATION_JSON),
                    analysisInventoryLiftingHandler::executeAnalysis
                )
                .POST("/disabled_rolls",
                    RequestPredicates.accept(MediaType.APPLICATION_JSON),
                    disableRollsHandler::disableUnliftedRolls
                )
            )
            .before(request->{
                    log.info("🔄 Router {} recibió solicitud: {} {}", PATH_REVISION,
                        request.method(), request.path());
                    return request;
            })
            .after((request, response)-> {
                log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_REVISION,
                    request.method(),
                    request.path(), response.statusCode());
                return response;
            })
            .build();
    }

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de revision tela en: /{}", PATH_REVISION);
    }

}
