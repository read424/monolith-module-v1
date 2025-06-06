package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.ApproveDeliveryHandler;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.OrdenIngresoLogisticaHandler;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.TransformacionInsumosHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterAlmacenReactiveAPI {
    private final OrdenIngresoLogisticaHandler ordenIngresoHandler;
    private final TransformacionInsumosHandler transformacionInsumosHandler;
    private final ApproveDeliveryHandler approveDeliveryHandler;
    private static final String PATH_ALMACEN="almacen";

    @Bean
    public RouterFunction<ServerResponse> almacenRouter(){
        return RouterFunctions.route()
            .path("/"+PATH_ALMACEN, builder -> builder
                .POST("/ingreso-logistica", RequestPredicates.accept(MediaType.APPLICATION_JSON), ordenIngresoHandler::nuevoIngresoLogistica)
                .POST("/transformacion", RequestPredicates.accept(MediaType.APPLICATION_JSON), transformacionInsumosHandler::crearConversion)
                .POST("/approve_delivery", RequestPredicates.accept(MediaType.APPLICATION_JSON), approveDeliveryHandler::deliver)
            )
            .before(request -> {
                log.info("🔄 Router {} recibió solicitud: {} {}", PATH_ALMACEN, request.method(), request.path());
                return request;
            })
            .after((request, response) -> {
                log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_ALMACEN, request.method(), request.path(), response.statusCode());
                return response;
            })
            .build();
    }

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de almacenes registradas en: /{}", PATH_ALMACEN);
    }
}
