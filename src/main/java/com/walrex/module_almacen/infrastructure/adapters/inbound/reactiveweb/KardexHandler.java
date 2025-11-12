package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.ConsultarKardexUseCase;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.ConsultarKardexRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ConsultarKardexRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.ReporteKardexResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class KardexHandler {
    private final Validator validator;
    private final ConsultarKardexUseCase consultarKardexUseCase;
    private final ConsultarKardexRequestMapper kardexRequestMapper;

    public Mono<ServerResponse> consultarKardex(ServerRequest request) {
        log.info("📊 Consultando kardex - Método HTTP: {}", request.method());
        log.info("🔍 Query params: {}", request.queryParams());

        return Mono.just(kardexRequestMapper.extractFromQuery(request))
                .doOnNext(dto->log.info("Request params recibidos: {}", dto))
                .flatMap(this::validate)
                .map(kardexRequestMapper::toCriterios)
                .flatMap(consultarKardexUseCase::consultarKardex)
                .map(kardexReporte -> ReporteKardexResponse.builder()
                        .data(kardexReporte.getArticulos())
                        .success(true)
                        .mensaje("Reporte de kardex generado exitosamente")
                        .build()
                )
                .flatMap(response -> ServerResponse.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                );
    }

    private Mono<ConsultarKardexRequest> validate(ConsultarKardexRequest dto) {
        var errors = new BeanPropertyBindingResult(dto, ConsultarKardexRequest.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            var errorMessages = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .toList();
            return Mono.error(new ServerWebInputException(String.join("; ", errorMessages)));
        }
        return Mono.just(dto);
    }
}
