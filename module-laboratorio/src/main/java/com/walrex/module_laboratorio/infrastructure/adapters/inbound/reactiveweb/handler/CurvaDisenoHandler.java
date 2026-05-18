package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.module_laboratorio.application.ports.input.CreateCurvaDisenoUseCase;
import com.walrex.module_laboratorio.application.ports.input.GenerateCurvaDisenoPdfUseCase;
import com.walrex.module_laboratorio.application.ports.input.GetCurvaDisenoByIdUseCase;
import com.walrex.module_laboratorio.application.ports.input.ListCurvaDisenoUseCase;
import com.walrex.module_laboratorio.domain.exceptions.CurvaDisenoException;
import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.CurvaDisenoCreateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper.CurvaDisenoRestMapper;
import com.walrex.module_security_commons.application.ports.UserContextProvider;
import com.walrex.module_security_commons.domain.model.JwtUserInfo;
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
public class CurvaDisenoHandler {

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final Validator validator;
    private final CreateCurvaDisenoUseCase createUseCase;
    private final GetCurvaDisenoByIdUseCase getByIdUseCase;
    private final ListCurvaDisenoUseCase listUseCase;
    private final GenerateCurvaDisenoPdfUseCase generatePdfUseCase;
    private final CurvaDisenoRestMapper mapper;
    private final UserContextProvider userContextProvider;

    public Mono<ServerResponse> create(ServerRequest request) {
        String idempotencyKey = request.headers().firstHeader(IDEMPOTENCY_KEY);
        JwtUserInfo userInfo = userContextProvider.getCurrentUser(request);

        return request.bodyToMono(CurvaDisenoCreateRequest.class)
                .flatMap(this::validate)
                .map(mapper::toDomain)
                .map(curvaDiseno -> assignLaboratorista(curvaDiseno, userInfo))
                .flatMap(curvaDiseno -> createUseCase.create(curvaDiseno, idempotencyKey))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        String search = request.queryParam("search").orElse("").trim();
        int page = parseQueryParam(request, "page", 1);
        int size = parseQueryParam(request, "size", 10);

        return listUseCase.listAll(search, page, size)
                .map(paged -> paged.map(mapper::toResponse))
                .flatMap(paged -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(paged))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return getByIdUseCase.getById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> generatePdf(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return generatePdfUseCase.generatePdf(id)
                .flatMap(pdfBytes -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", "attachment; filename=\"curva-diseno-" + id + ".pdf\"")
                        .bodyValue(pdfBytes))
                .onErrorResume(this::buildErrorResponse);
    }

    private CurvaDiseno assignLaboratorista(CurvaDiseno curvaDiseno, JwtUserInfo userInfo) {
        try {
            curvaDiseno.setIdLaboratorista(Integer.valueOf(userInfo.getUserId()));
            return curvaDiseno;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new CurvaDisenoException("No se pudo obtener el id del laboratorista autenticado", "INVALID_USER");
        }
    }

    private int parseQueryParam(ServerRequest request, String name, int defaultValue) {
        return request.queryParam(name)
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    private Mono<CurvaDisenoCreateRequest> validate(CurvaDisenoCreateRequest dto) {
        var errors = new BeanPropertyBindingResult(dto, CurvaDisenoCreateRequest.class.getName());
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
        if (error instanceof CurvaDisenoException curvaDisenoException) {
            return switch (curvaDisenoException.getCode()) {
                case "NOT_FOUND" -> ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(curvaDisenoException.getMessage());
                case "IDEMPOTENCY_CONFLICT" -> ServerResponse.status(HttpStatus.CONFLICT).bodyValue(curvaDisenoException.getMessage());
                case "INVALID_CURVA_DISENO", "CURVA_DISENO_EMPTY" ->
                        ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(curvaDisenoException.getMessage());
                case "PDF_GENERATION_ERROR" ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(curvaDisenoException.getMessage());
                case "IDEMPOTENCY_KEY_REQUIRED", "INVALID_USER" ->
                        ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(curvaDisenoException.getMessage());
                default -> ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(curvaDisenoException.getMessage());
            };
        }

        if (error instanceof ServerWebInputException || error instanceof NumberFormatException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(error.getMessage());
        }

        log.error("Error procesando curva de diseño: {}", error.getMessage(), error);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error interno al procesar la solicitud");
    }
}
