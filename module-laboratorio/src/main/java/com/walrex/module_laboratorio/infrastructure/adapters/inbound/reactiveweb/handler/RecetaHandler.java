package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.module_laboratorio.application.ports.input.ListRecetasUseCase;
import com.walrex.module_laboratorio.domain.exceptions.RecetaException;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.UpdateCurvaDisenoRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper.RecetaCurvaDisenoMapper;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper.RecetaRestMapper;
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
public class RecetaHandler {

    private final Validator validator;
    private final ListRecetasUseCase listRecetasUseCase;
    private final RecetaRestMapper mapper;
    private final RecetaCurvaDisenoMapper recetaCurvaDisenoMapper;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        String search = request.queryParam("search").orElse("").trim();
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));

        return listRecetasUseCase.listAll(search, page, size)
                .map(paged -> paged.map(mapper::toResponse))
                .flatMap(paged -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(paged))
                .onErrorResume(e -> {
                    log.error("Error listando recetas: {}", e.getMessage(), e);
                    return ServerResponse.status(500)
                            .bodyValue("Error interno al procesar la solicitud");
                });
    }

    public Mono<ServerResponse> updateCurvaDiseno(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));

        return request.bodyToMono(UpdateCurvaDisenoRequest.class)
                .flatMap(this::validate)
                .flatMap(body -> listRecetasUseCase.updateCurvaDiseno(id, body.getCurvaDiseno().toString()))
                .map(recetaCurvaDisenoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> getCurvaDisenoById(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));

        return listRecetasUseCase.getCurvaDisenoById(id)
                .map(recetaCurvaDisenoMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> generateCurvaDisenoPdf(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));

        return listRecetasUseCase.generateCurvaDisenoPdf(id)
                .flatMap(pdfBytes -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", "attachment; filename=\"receta-" + id + "-curva-diseno.pdf\"")
                        .bodyValue(pdfBytes))
                .onErrorResume(this::buildErrorResponse);
    }

    private Mono<UpdateCurvaDisenoRequest> validate(UpdateCurvaDisenoRequest dto) {
        var errors = new BeanPropertyBindingResult(dto, UpdateCurvaDisenoRequest.class.getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            String message = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .reduce((left, right) -> left + "; " + right)
                    .orElse("Solicitud inválida");
            return Mono.error(new ServerWebInputException(message));
        }
        return Mono.just(dto);
    }

    private Mono<ServerResponse> buildErrorResponse(Throwable error) {
        if (error instanceof RecetaException recetaException) {
            return switch (recetaException.getCode()) {
                case "NOT_FOUND" -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(recetaException.getMessage());
                case "INVALID_CURVA_DISENO" -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(recetaException.getMessage());
                case "CURVA_DISENO_EMPTY" -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(recetaException.getMessage());
                case "PDF_GENERATION_ERROR" -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(recetaException.getMessage());
                default -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(recetaException.getMessage());
            };
        }

        if (error instanceof ServerWebInputException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(error.getMessage());
        }

        log.error("Error actualizando curva_diseno de receta: {}", error.getMessage(), error);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error interno al procesar la solicitud");
    }
}
