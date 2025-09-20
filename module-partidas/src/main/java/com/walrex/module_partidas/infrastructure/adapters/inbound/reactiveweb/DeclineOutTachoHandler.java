package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_partidas.application.ports.input.DeclineOutTachoUseCase;
import com.walrex.module_partidas.domain.model.DeclinePartidaTacho;
import com.walrex.module_partidas.domain.model.JwtUserInfo;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.DeclineOutTachoMapper;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.DeclineSalidaTachoMapper;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.DeclineSalidaTachoRequest;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.DeclineOutTachoResponse;
import com.walrex.module_partidas.infrastructure.adapters.inbound.rest.JwtUserPartidaContextService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para el endpoint declineOutTacho
 * Maneja las solicitudes POST /partidas/decline-out-tacho
 *
*/
@Slf4j
@Component
@RequiredArgsConstructor
public class DeclineOutTachoHandler {

    private final DeclineOutTachoUseCase declineOutTachoUseCase;
    private final DeclineSalidaTachoMapper declineSalidaTachoMapper;
    private final DeclineOutTachoMapper declineOutTachoMapper;
    private final Validator validator;
    private final JwtUserPartidaContextService jwtService;

    /**
     * Maneja la solicitud POST para declinar salida de tacho
     *
     * @param request Solicitud del servidor
     * @return Respuesta del servidor
     */
    public Mono<ServerResponse> declineOutTacho(ServerRequest request) {
        JwtUserInfo user = jwtService.getCurrentUser(request);
        log.info("Recibida solicitud POST para declineOutTacho");

        return request.bodyToMono(DeclineSalidaTachoRequest.class)
                .doOnNext(declineSalidaTacho -> log.info("Datos recibidos: {}",
                        declineSalidaTacho))
                .map(declineSalidaTachoMapper::toDomain)
                .doOnNext(domain -> {
                    domain.setIdUsuario(Integer.valueOf(user.getUserId()));
                    log.info("ID usuario seteado: {}", user.getUserId());
                })
                .flatMap(this::validateDomain)
                .flatMap(declineOutTachoUseCase::declineOutTacho)
                .map(ingresoAlmacenDTO -> {
                    // Mapear a respuesta con información adicional de rechazo
                    DeclineOutTachoResponse response = declineOutTachoMapper.toDeclineOutTachoResponse(ingresoAlmacenDTO);
                    
                    // Obtener información del dominio para completar la respuesta
                    // Nota: En un caso real, podrías necesitar pasar esta información de otra manera
                    // Por ahora, se dejan como null y se pueden setear en el service si es necesario
                    response.setMotivoRechazo("Motivo de rechazo registrado");
                    response.setPersonalSupervisor("Personal supervisor registrado");
                    response.setObservacion("Observación de rechazo registrada");
                    
                    return response;
                })
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.info("Respuesta exitosa enviada para declineOutTacho"))
                .doOnError(error -> log.error("Error procesando declineOutTacho: {}", error.getMessage()))
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
    private Mono<DeclinePartidaTacho> validateDomain(DeclinePartidaTacho domain) {
        Set<ConstraintViolation<DeclinePartidaTacho>> violations = validator.validate(domain);

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
