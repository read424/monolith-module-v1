package com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb.router;

import com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb.PersonalHandler;
import com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb.UserGetDetailsHandler;
import com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb.UserSignInHandler;
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
@RequiredArgsConstructor
@Slf4j
public class RouterReactiveAPI {
    private static final String PATH_LOGIN = "user";
    private final UserSignInHandler userSigninHandler;
    private final UserGetDetailsHandler userGetDetailsHandler;
    private final PersonalHandler personalHandler;

    @Bean
    public RouterFunction<ServerResponse> userRouter(){
        return RouterFunctions.route()
            .path( "/auth", builder -> builder
                .POST("/signin", RequestPredicates.accept(MediaType.APPLICATION_JSON), userSigninHandler::signInUser)
                .POST("/recovery-password",RequestPredicates.accept(MediaType.APPLICATION_JSON), userGetDetailsHandler::senderMailRecoveryPassword)
            )
            .path("/"+PATH_LOGIN, builder -> builder
                .GET("/list", RequestPredicates.accept(MediaType.APPLICATION_JSON), personalHandler::listPersonal)
                .GET("/{name_user}", RequestPredicates.accept(MediaType.APPLICATION_JSON), userGetDetailsHandler::getInfoUser)
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
        log.info("🔌 Rutas del módulo de usuarios registradas en: /{}", PATH_LOGIN);
    }
}
