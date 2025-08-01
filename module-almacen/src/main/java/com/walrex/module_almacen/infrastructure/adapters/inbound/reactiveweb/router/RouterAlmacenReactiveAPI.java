package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.*;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterAlmacenReactiveAPI {
        private final OrdenIngresoLogisticaHandler ordenIngresoHandler;
        private final TransformacionInsumosHandler transformacionInsumosHandler;
        private final ApproveDeliveryHandler approveDeliveryHandler;
        private final KardexHandler kardexHandler;
        private final KardexExcelHandler kardexExcelHandler;
        private final RollosDevolucionHandler rollosDevolucionHandler;
        private final OrdenSalidaDevolucionHandler ordenSalidaDevolucionHandler;
        private final GuiaRemisionHandler generarGuiaRemisionHandler;
        private static final String PATH_ALMACEN = "almacen";

        @Bean
        public RouterFunction<ServerResponse> almacenRouter() {
                return RouterFunctions.route()
                                .path("/" + PATH_ALMACEN, builder -> builder
                                                .POST("/ingreso-logistica",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                ordenIngresoHandler::nuevoIngresoLogistica)
                                                .POST("/transformacion",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                transformacionInsumosHandler::crearConversion)
                                                .POST("/approve_delivery",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                approveDeliveryHandler::deliver)
                                                .GET("/kardex", RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                kardexHandler::consultarKardex)
                                                .GET("/kardex/excel", RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                kardexExcelHandler::exportarKardexExcel)
                                                .GET("/rollos-disponibles-devolucion",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                rollosDevolucionHandler::consultarRollosDisponibles)
                                                .POST("/devolucion-rollos",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                rollosDevolucionHandler::crearDevolucionRollos)
                                                .GET("/ordenes-salida-devolucion",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                ordenSalidaDevolucionHandler::consultarOrdenSalidaDevolucion)
                                                .POST("/generar-guia",
                                                                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                                                                generarGuiaRemisionHandler::generarGuiaRemision))
                                .before(request -> {
                                        log.info("🔄 Router {} recibió solicitud: {} {}", PATH_ALMACEN,
                                                        request.method(), request.path());
                                        return request;
                                })
                                .after((request, response) -> {
                                        log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_ALMACEN,
                                                        request.method(),
                                                        request.path(), response.statusCode());
                                        return response;
                                })
                                .build();
        }

        @PostConstruct
        public void init() {
                log.info("🔌 Rutas del módulo de almacenes registradas en: /{}", PATH_ALMACEN);
        }
}
