package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_partidas.application.ports.input.DeclararProcesoIncompletoUseCase;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.ProcesoDeclararMapper;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request.ProcesosPartidaIncompletosRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
import com.walrex.module_security_commons.infrastructure.adapters.JwtUserContextService;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeclararProcesosHandler {

    private final DeclararProcesoIncompletoUseCase declararProcesoIncompletoUseCase;
    private final JwtUserContextService jwtService;
    private final Validator validator;
    private final ProcesoDeclararMapper mapper;

    public Mono<ServerResponse> procesarDeclaracionesIncompletas(ServerRequest request){
        JwtUserInfo user = jwtService.getCurrentUser(request);

        return request.bodyToMono(ProcesosPartidaIncompletosRequest.class)
            .flatMap(this::validarRequest)
            .flatMap(validRequest -> {
                log.info("Procesando declaraciones incompletas para partida: {}", validRequest.getIdPartida());
                log.info("Cantidad de procesos a procesar: {}", validRequest.getProcesos().length);

                // Convertir request a DTOs de dominio
                var procesosDTO = mapper.fromRequest(validRequest);

                // Llamar al use case con los parámetros correctos
                return declararProcesoIncompletoUseCase.registrarDeclaracionProceso(
                    procesosDTO,
                    validRequest.getIdPartida()
                )
                .flatMap(procesosProcesados ->
                    ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                            "mensaje", "Procesos declarados exitosamente",
                            "id_partida", validRequest.getIdPartida(),
                            "procesos_procesados", procesosProcesados
                        ))
                );
            })
            .onErrorResume(this::handleValidationError);
    }

    private Mono<ProcesosPartidaIncompletosRequest> validarRequest(ProcesosPartidaIncompletosRequest request) {
        Set<ConstraintViolation<ProcesosPartidaIncompletosRequest>> violations = validator.validate(request);

        if (violations.isEmpty()) {
            return Mono.just(request);
        }

        String errores = violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));

        log.error("Errores de validación: {}", errores);
        return Mono.error(new IllegalArgumentException(errores));
    }

    private Mono<ServerResponse> handleValidationError(Throwable error) {
        log.error("Error procesando declaraciones: {}", error.getMessage());

        return ServerResponse.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "error", "Error de validación",
                "mensaje", error.getMessage()
            ));
    }
}
