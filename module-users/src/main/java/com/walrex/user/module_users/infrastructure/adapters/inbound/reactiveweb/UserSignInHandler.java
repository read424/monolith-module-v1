package com.walrex.user.module_users.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.user.module_users.application.ports.input.SignInUserCase;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginRequestDto;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.LoginResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.server.ServerWebInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSignInHandler {
    private final SignInUserCase signInUserCase;
    private final Validator validator;

    public Mono<ServerResponse> signInUser(ServerRequest request){
        log.info("Método HTTP: {}", request.method());
        log.info("Headers: {}", request.headers().asHttpHeaders());

        return request.bodyToMono(LoginRequestDto.class)
            .doOnNext(dto->log.info("Request body recibido: {}", dto))
            .flatMap(this::validate)
            .flatMap( req -> ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(signInUserCase.validUserPassword(req), LoginResponseDTO.class)
            );
    }

    private Mono<LoginRequestDto> validate(LoginRequestDto dto) {
        var errors = new BeanPropertyBindingResult(dto, LoginRequestDto.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            // Construir mensaje de error
            var errorMessages = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .toList();
            return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
        }
        return Mono.just(dto);
    }
}
