package com.walrex.module_articulos.infrastructure.adapters.inbound.grahpql.router;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.webflux.GraphQlHttpHandler;
import org.springframework.graphql.server.webflux.GraphQlWebSocketHandler;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RouterGraphQLAPI {
    private final GraphQlHttpHandler graphQlHttpHandler;
    //private final GraphQlWebSocketHandler graphQlWebSocketHandler;

    private static final String PATH_GRAPHQL = "graphql";
    private static final String PATH_GRAPHQL_WS = "graphql/ws";

    @Bean
    public RouterFunction<ServerResponse> graphQLRouter(){
        return RouterFunctions.route()
            .path("/"+PATH_GRAPHQL, builder->builder
                .POST("", RequestPredicates.accept(MediaType.APPLICATION_JSON),
                    request -> graphQlHttpHandler.handleRequest(request)
                )
            )
            .before(request->{
                log.info("🔄 GraphQL Router recibió solicitud: {} {}", request.method(), request.path());
                return request;
            })
            .after((request, response) -> {
                log.info("✅ GraphQL Router respondió a: {} {} con estado: {}", request.method(), request.path(), response.statusCode());
                return response;
            })
            .build();
    }

    // Para soportar suscripciones GraphQL a través de WebSocket
    /*
    @Bean
    public SimpleUrlHandlerMapping graphQLWebSocketMapping() {
        Map<String, WebSocketHandler> urlMap = new HashMap<>();
        urlMap.put("/" + PATH_GRAPHQL_WS, graphQlWebSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(urlMap);
        mapping.setOrder(-1); // antes que handlers de WebFlux
        return mapping;
    }
     */

    // Necesario para que funcione el WebSocketHandler
    /*
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
     */

    @PostConstruct
    public void init() {
        log.info("🔌 GraphQL API configurada y disponible en: /{}", PATH_GRAPHQL);
        log.info("📊 GraphiQL (interfaz de desarrollo) disponible en: /graphiql");
    }
}
