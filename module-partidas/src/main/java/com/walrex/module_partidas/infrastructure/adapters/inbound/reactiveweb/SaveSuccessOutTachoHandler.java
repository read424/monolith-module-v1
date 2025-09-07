package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_partidas.application.ports.input.SaveSuccessOutTachoUseCase;
import com.walrex.module_partidas.domain.model.JwtUserInfo;
import com.walrex.module_partidas.domain.model.SuccessPartidaTacho;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.SalidaTachoMapper;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.SuccessOutTachoMapper;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.SavedSalidaTacho;
import com.walrex.module_partidas.infrastructure.adapters.inbound.rest.JwtUserPartidaContextService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para el endpoint saveSuccessOutTacho
 * Maneja las solicitudes POST /partida/out-success-tacho
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaveSuccessOutTachoHandler {

    private final SaveSuccessOutTachoUseCase saveSuccessOutTachoUseCase;
    private final SalidaTachoMapper salidaTachoMapper;
    private final Validator validator;
    private final JwtUserPartidaContextService jwtService;
    private final SuccessOutTachoMapper successOutTachoMapper;

    /**
     * Maneja la solicitud POST para guardar el éxito de salida de tacho
     *
     * @param request Solicitud del servidor
     * @return Respuesta del servidor
     */
    public Mono<ServerResponse> saveSuccessOutTacho(ServerRequest request) {
        JwtUserInfo user = jwtService.getCurrentUser(request);
        log.info("Recibida solicitud POST para saveSuccessOutTacho");

        return request.bodyToMono(SavedSalidaTacho.class)
                .doOnNext(savedSalidaTacho -> log.info("Datos recibidos: {}",
                        savedSalidaTacho))
                .map(salidaTachoMapper::toDomain)
                .doOnNext(domain -> {
                    if(domain.getIdSupervisor() == null){
                        domain.setIdSupervisor(Integer.valueOf(user.getUserId()));
                    }
                    log.info("ID supervisor seteado: {}", user.getUserId());
                })
                .flatMap(this::validateDomain)
                .flatMap(saveSuccessOutTachoUseCase::saveSuccessOutTacho)
                .map(successOutTachoMapper::toSuccessOutTachoResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.info("Respuesta exitosa enviada para saveSuccessOutTacho"))
                .doOnError(error -> log.error("Error procesando saveSuccessOutTacho: {}", error.getMessage()))
                .onErrorResume(IllegalArgumentException.class, error -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new ErrorResponse("Error de validación: " + error.getMessage())))
                .onErrorResume(Exception.class, error -> ServerResponse.status(500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(new ErrorResponse("Error interno del servidor: " + error.getMessage())));
    }

    /**
     * Valida los datos del dominio usando Bean Validation
     *
     * @param domain Modelo de dominio a validar
     * @return Mono con el dominio validado o error
     */
    private Mono<SuccessPartidaTacho> validateDomain(SuccessPartidaTacho domain) {
        Set<ConstraintViolation<SuccessPartidaTacho>> violations = validator.validate(domain);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Error de validación");

            log.error("Errores de validación encontrados: {}", errorMessage);
            return Mono.error(new IllegalArgumentException(errorMessage));
        }

        log.info("Validación exitosa para partida ID: {}", domain.getIdPartida());
        return Mono.just(domain);
    }

    /**
     * DTO de respuesta exitosa
     */
    public static class SuccessResponse {
        private final String message;
        private final String status = "success";

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * DTO de respuesta de error
     */
    public static class ErrorResponse {
        private final String message;
        private final String status = "error";

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }
    }
}
