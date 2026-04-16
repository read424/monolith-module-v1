package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.handler;

import com.walrex.module_laboratorio.application.ports.input.CreateProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.input.DeleteProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.input.GetProductoEventoByIdUseCase;
import com.walrex.module_laboratorio.application.ports.input.ListProductoEventoUseCase;
import com.walrex.module_laboratorio.application.ports.input.UpdateProductoEventoUseCase;
import com.walrex.module_laboratorio.domain.exceptions.ProductoEventoException;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.ProductoEventoCreateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.ProductoEventoSearchRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.ProductoEventoUpdateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper.ProductoEventoRestMapper;
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

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductoEventoHandler {

    private final Validator validator;
    private final CreateProductoEventoUseCase createUseCase;
    private final GetProductoEventoByIdUseCase getByIdUseCase;
    private final ListProductoEventoUseCase listUseCase;
    private final UpdateProductoEventoUseCase updateUseCase;
    private final DeleteProductoEventoUseCase deleteUseCase;
    private final ProductoEventoRestMapper mapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(ProductoEventoCreateRequest.class)
                .flatMap(this::validate)
                .map(mapper::toDomain)
                .flatMap(createUseCase::create)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.created(URI.create("/laboratorio/producto-evento/" + response.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
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

    public Mono<ServerResponse> findAll(ServerRequest request) {
        ProductoEventoSearchRequest searchRequest = ProductoEventoSearchRequest.builder()
                .search(request.queryParam("search").orElse("").trim())
                .page(Integer.valueOf(request.queryParam("page").orElse("0")))
                .size(Integer.valueOf(request.queryParam("size").orElse("10")))
                .status(request.queryParam("status").map(Integer::valueOf).orElse(null))
                .build();

        return validate(searchRequest)
                .flatMap(validRequest -> listUseCase.listAll(
                        validRequest.getSearch(),
                        validRequest.getPage(),
                        validRequest.getSize(),
                        validRequest.getStatus()))
                .map(mapper::toPaginatedResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return request.bodyToMono(ProductoEventoUpdateRequest.class)
                .flatMap(this::validate)
                .map(mapper::toDomain)
                .flatMap(productoEvento -> updateUseCase.update(id, productoEvento))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(this::buildErrorResponse);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Integer id = Integer.valueOf(request.pathVariable("id"));
        return deleteUseCase.delete(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(this::buildErrorResponse);
    }

    private <T> Mono<T> validate(T dto) {
        var errors = new BeanPropertyBindingResult(dto, dto.getClass().getName());
        validator.validate(dto, errors);
        if (errors.hasErrors()) {
            String message = errors.getFieldErrors().stream()
                    .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                    .distinct()
                    .reduce((left, right) -> left + "; " + right)
                    .orElse("Solicitud inválida");
            return Mono.error(new ServerWebInputException(message));
        }

        if (dto instanceof ProductoEventoUpdateRequest updateRequest
                && updateRequest.getNombre() != null
                && updateRequest.getNombre().isBlank()) {
            return Mono.error(new ServerWebInputException("Campo 'nombre': El nombre no puede estar en blanco"));
        }

        return Mono.just(dto);
    }

    private Mono<ServerResponse> buildErrorResponse(Throwable error) {
        if (error instanceof ProductoEventoException productoEventoException) {
            return switch (productoEventoException.getCode()) {
                case "NOT_FOUND" -> ServerResponse.status(HttpStatus.NOT_FOUND)
                        .bodyValue(productoEventoException.getMessage());
                case "DUPLICATE_NAME" -> ServerResponse.status(HttpStatus.CONFLICT)
                        .bodyValue(productoEventoException.getMessage());
                case "INVALID_NAME" -> ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .bodyValue(productoEventoException.getMessage());
                default -> ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .bodyValue(productoEventoException.getMessage());
            };
        }

        if (error instanceof ServerWebInputException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(error.getMessage());
        }

        log.error("Error inesperado en ProductoEventoHandler: {}", error.getMessage(), error);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error interno al procesar la solicitud");
    }
}
