package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.router;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.MotivosDevolucionHandler;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.CrearMotivoDevolucionRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.MotivoDevolucionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Router para configurar las rutas de motivos de devolución
 * Siguiendo el patrón WebFlux: RouterFunction + Handler
 */
@Configuration
@Slf4j
public class RouterMotivosDevolucionReactiveAPI {

    private static final String PATH_ALMACEN = "almacen";

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/almacen/motivos-devolucion",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            beanClass = MotivosDevolucionHandler.class,
            beanMethod = "obtenerMotivosActivos",
            operation = @Operation(
                operationId = "obtenerMotivosActivos",
                summary = "Obtener motivos de devolución activos",
                description = "Obtiene la lista completa de motivos de devolución que están activos en el sistema",
                tags = {"Motivos de Devolución"},
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Lista de motivos obtenida exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MotivoDevolucionResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Error al obtener motivos de devolución"
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/almacen/motivos-devolucion/buscar",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            beanClass = MotivosDevolucionHandler.class,
            beanMethod = "buscarMotivosPorDescripcion",
            operation = @Operation(
                operationId = "buscarMotivosPorDescripcion",
                summary = "Buscar motivos por descripción",
                description = "Busca motivos de devolución que contengan el texto especificado en su descripción",
                tags = {"Motivos de Devolución"},
                parameters = {
                    @Parameter(
                        name = "q",
                        in = ParameterIn.QUERY,
                        description = "Texto a buscar en la descripción del motivo",
                        required = false,
                        example = "defecto",
                        schema = @Schema(type = "string")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Búsqueda realizada exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MotivoDevolucionResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Error al buscar motivos"
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/almacen/motivos-devolucion",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            beanClass = MotivosDevolucionHandler.class,
            beanMethod = "crearMotivoDevolucion",
            operation = @Operation(
                operationId = "crearMotivoDevolucion",
                summary = "Crear nuevo motivo de devolución",
                description = "Crea un nuevo motivo de devolución con la descripción especificada",
                tags = {"Motivos de Devolución"},
                requestBody = @RequestBody(
                    description = "Datos del motivo de devolución a crear",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CrearMotivoDevolucionRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Motivo de devolución creado exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MotivoDevolucionResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Error al crear motivo de devolución (datos inválidos o descripción duplicada)"
                    )
                }
            )
        )
    })
    public RouterFunction<ServerResponse> motivosDevolucionRoutes(MotivosDevolucionHandler motivosDevolucionHandler) {
        return RouterFunctions.route()
                .path("/" + PATH_ALMACEN, builder -> builder
                    .GET("/motivos-devolucion",
                        RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        motivosDevolucionHandler::obtenerMotivosActivos)
                    .GET("/motivos-devolucion/buscar",
                        RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        motivosDevolucionHandler::buscarMotivosPorDescripcion)
                    .POST("/motivos-devolucion",
                        RequestPredicates.accept(MediaType.APPLICATION_JSON),
                        motivosDevolucionHandler::crearMotivoDevolucion)
                ).before(request -> {
                    log.info("🔄 Router {} recibió solicitud: {} {}", PATH_ALMACEN,
                        request.method(), request.path());
                    return request;
                })
                .after((request, response) -> {
                    log.info("✅ Router {} respondió a: {} {} con estado: {}", PATH_ALMACEN,
                        request.method(),
                        request.path(), response.statusCode());
                    return response;
                })
                .build();
    }
}
