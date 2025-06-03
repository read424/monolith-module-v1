package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.ApproveDeliveryHandler;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.OrdenIngresoLogisticaHandler;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.TransformacionInsumosHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

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
                .POST("", RequestPredicates.accept(MediaType.APPLICATION_JSON), ordenIngresoHandler::nuevoIngresoLogistica)
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
}
