package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.*;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Router reactivo para la API de Partidas
 * Configura las rutas usando RouterFunction para WebFlux
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterPartidasReactiveAPI {

    private final AlmacenTachoHandler almacenTachoHandler;
    private final DetalleIngresoHandler detalleIngresoHandler;
    private final SaveSuccessOutTachoHandler saveSuccessOutTachoHandler;
    private final DeclineOutTachoHandler declineOutTachoHandler;
    private final DeclararProcesosHandler declararProcesosHandler;
    private static final String PATH_PARTIDAS = "partidas";

    /**
     * Configura las rutas para la API de Partidas
     *
     * Endpoints disponibles:
     * - POST /partidas/out-tacho - Guardar éxito de salida de tacho
     * - POST /partidas/decline-out-tacho - Declinar salida de tacho
     * - GET /partidas/almacen-tacho - Consultar almacén tacho con filtros
     * - GET /partidas/detalle-ingreso - Consultar detalle de ingreso con rollos
     * - POST /partidas/declarar-procesos-incompletos -
     *
     * @return RouterFunction configurado
     */
    @Bean
    public RouterFunction<ServerResponse> partidasRouter() {
        return RouterFunctions.route()
                .path("/" + PATH_PARTIDAS, builder -> builder
                        .GET("/almacen-tacho", RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                almacenTachoHandler::consultarAlmacenTacho)
                        .POST("/out-tacho",
                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                saveSuccessOutTachoHandler::saveSuccessOutTacho)
                        .POST("/decline-out-tacho",
                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                declineOutTachoHandler::declineOutTacho)
                        .GET("/detalle-ingreso",
                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                detalleIngresoHandler::consultarDetalleIngreso)
                        .POST("/declarar-procesos-incompletos",
                            RequestPredicates.accept(MediaType.APPLICATION_JSON),
                            declararProcesosHandler::procesarDeclaracionesIncompletas)
                )
                .before(request -> {
                    log.info("🔄 Router {} recibió solicitud: {} {}",
                            PATH_PARTIDAS,
                            request.method(), request.path());
                    return request;
                })
                .after((request, response) -> {
                    log.info("✅ Router {} respondió a: {} {} con estado: {}",
                            PATH_PARTIDAS,
                            request.method(),
                            request.path(), response.statusCode());
                    return response;
                })
                .build();
    }

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de almacenes registradas en: /{}", PATH_PARTIDAS);
    }
}
