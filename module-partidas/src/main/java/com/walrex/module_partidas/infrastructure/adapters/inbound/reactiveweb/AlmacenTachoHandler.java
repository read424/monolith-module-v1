package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_partidas.application.ports.input.ConsultarAlmacenTachoUseCase;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper.AlmacenTachoResponseMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para el endpoint de almacén tacho
 * Maneja las peticiones HTTP y valida los datos de entrada
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlmacenTachoHandler {

    private final ConsultarAlmacenTachoUseCase consultarAlmacenTachoUseCase;
    private final AlmacenTachoResponseMapper almacenTachoResponseMapper;
    private final Validator validator;

    /**
     * Consulta almacén tacho
     *
     * @param request Petición del servidor
     * @return Respuesta del servidor con los datos
     */
    public Mono<ServerResponse> consultarAlmacenTacho(ServerRequest request) {
        log.info("Recibida petición para consultar almacén tacho");

        // Leer parámetros desde query params
        Integer idAlmacen = request.queryParam("id_almacen")
                .map(Integer::valueOf)
                .orElse(null);
        
        Integer page = request.queryParam("page")
                .map(Integer::valueOf)
                .orElse(0);
        
        Integer numRows = request.queryParam("num_rows")
                .map(Integer::valueOf)
                .orElse(10);
        
        String codPartida = request.queryParam("cod_partida").orElse("");

        log.info("Parámetros recibidos - Almacén: {}, Página: {}, Filas: {}, Código Partida: {}", 
                idAlmacen, page, numRows, codPartida);

        // Crear el request con los parámetros
        var consultarRequest = ConsultarAlmacenTachoRequest.builder()
                .idAlmacen(idAlmacen)
                .page(page)
                .numRows(numRows)
                .codPartida(codPartida)
                .build();

        // Validar el request
        Set<ConstraintViolation<ConsultarAlmacenTachoRequest>> violations = validator.validate(consultarRequest);
        if (!violations.isEmpty()) {
            return ServerResponse.badRequest()
                    .bodyValue("Error de validación: " + violations.iterator().next().getMessage());
        }

        return consultarAlmacenTachoUseCase.listarPartidasInTacho(consultarRequest)
            .map(almacenTachoResponseMapper::toListPartidaTachoResponse)
            .flatMap(response -> {
                log.info("Consulta completada - Total: {}, Páginas: {}, Página actual: {}", 
                        response.getTotalRecords(), response.getTotalPages(), response.getCurrentPage());
                
                return ServerResponse.ok()
                        .bodyValue(response);
            })
            .onErrorResume(error -> {
                log.error("Error en consulta de almacén tacho: {}", error.getMessage(), error);
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error interno del servidor: " + error.getMessage());
            });
    }
}
