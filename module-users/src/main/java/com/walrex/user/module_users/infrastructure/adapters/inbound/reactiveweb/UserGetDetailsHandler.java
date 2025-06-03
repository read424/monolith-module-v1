package com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.user.module_users.application.ports.input.GetUserDetailsUseCase;
import com.walrex.user.module_users.application.ports.input.RecoveryPasswordUseCase;
import com.walrex.user.module_users.domain.exception.EmailNotFoundException;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.RecoveryPasswordRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserGetDetailsHandler {
    private final GetUserDetailsUseCase getUserDetailsUseCase;
    private final RecoveryPasswordUseCase recoveryPasswordUseCase;

    public Mono<ServerResponse> getInfoUser(ServerRequest request){
        return Mono.justOrEmpty(request.pathVariable("name_user"))
                .filter(nameUser -> !nameUser.isBlank())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El parametro 'name_user' es obligatorio  y no puede estar vacío")))
                .flatMap(nameUser-> getUserDetailsUseCase.getUserDetails(nameUser)
                                .flatMap(userDetailsResponseDTO -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(userDetailsResponseDTO)
                                )
                )
                .onErrorResume(e -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", e.getMessage()))
                );
    }

    public Mono<ServerResponse> senderMailRecoveryPassword(ServerRequest request) {
        return request.bodyToMono(RecoveryPasswordRequestDTO.class)
                .flatMap(requestDTO -> {
                    if (requestDTO.getEmail() == null || requestDTO.getEmail().isBlank()) {
                        return Mono.error(new IllegalArgumentException("El email es obligatorio"));
                    }
                    return recoveryPasswordUseCase.initiatePasswordRecovery(requestDTO.getEmail());
                })
                .flatMap(result -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("message", "Se ha enviado un código de recuperación a tu email"))
                )
                .onErrorResume(e -> {
                    if (e instanceof EmailNotFoundException) {
                        return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", "El email proporcionado no está registrado"));
                    }
                    return ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of("error", e.getMessage()));
                });
    }
}