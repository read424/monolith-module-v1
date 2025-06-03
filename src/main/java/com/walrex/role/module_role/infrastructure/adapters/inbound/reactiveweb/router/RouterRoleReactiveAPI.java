package com.walrex.role.module_role.infrastructure.adapters.inbound.reactiveweb.router;

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

@Configuration
@Slf4j
public class RouterRoleReactiveAPI {
    private static final String PATH_ROLE = "role";

    @Bean
    public RouterFunction<ServerResponse> roleRouter(){
        return RouterFunctions.route()
                .path("/"+PATH_ROLE, builder -> builder
                        .GET("/",RequestPredicates.accept(MediaType.APPLICATION_JSON), request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("{\"status\": \"module-role-enabled\"}")
                        )
                )
                .before(request -> {
                    log.info("🔄 Router role recibió solicitud: {} {}", request.method(), request.path());
                    return request;
                })
                .after((request, response) -> {
                    log.info("✅ Router role respondió a: {} {} con estado: {}", request.method(), request.path(), response.statusCode());
                    return response;
                })
                .build();
    }

    @PostConstruct
    public void init() {
        log.info("🔌 Rutas del módulo de role registradas en: /{}", PATH_ROLE);
    }

}
