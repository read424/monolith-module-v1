package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import com.walrex.module_almacen.application.ports.input.ConsultarOrdenSalidaDevolucionUseCase;
import com.walrex.module_almacen.domain.model.PaginationDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ListadoOrdenSalidaDevolucionRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.OrdenSalidaDevolucionResponse;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Handler reactivo para las operaciones relacionadas con órdenes de salida por devolución.
 * Maneja los endpoints para consultar el listado de órdenes de salida por devolución.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Órdenes de Salida por Devolución", description = "Endpoints para consultar órdenes de salida por devolución")
public class OrdenSalidaDevolucionHandler {
    
    private final ConsultarOrdenSalidaDevolucionUseCase consultarOrdenSalidaDevolucionUseCase;
    
    /**
     * Consulta el listado de órdenes de salida por devolución aplicando filtros opcionales.
     * 
     * @param request el request con los parámetros de consulta
     * @return Mono con la respuesta del servidor
     */
    @Operation(
        summary = "Consultar listado paginado de órdenes de salida por devolución",
        description = "Obtiene el listado paginado de órdenes de salida por devolución aplicando filtros opcionales como nombre del cliente, rango de fechas, código de salida y número de guía con soporte de paginación y cache",
        parameters = {
            @Parameter(name = "nombreCliente", description = "Nombre del cliente para filtrar", example = "Juan Pérez"),
            @Parameter(name = "fechaInicio", description = "Fecha de inicio (yyyy-MM-dd)", example = "2024-01-01"),
            @Parameter(name = "fechaFin", description = "Fecha de fin (yyyy-MM-dd)", example = "2024-12-31"),
            @Parameter(name = "codigoSalida", description = "Código de salida para filtrar", example = "SAL-2024-001"),
            @Parameter(name = "numeroGuia", description = "Número de guía para filtrar", example = "GU-001"),
            @Parameter(name = "page", description = "Número de página (basado en 0)", example = "0"),
            @Parameter(name = "size", description = "Tamaño de página (1-100)", example = "10"),
            @Parameter(name = "sortBy", description = "Campo para ordenar: id_ordensalida, cod_salida, fec_entrega", example = "id_ordensalida"),
            @Parameter(name = "sortDirection", description = "Dirección de ordenamiento: ASC, DESC", example = "DESC")
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado paginado obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaginatedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de consulta o paginación inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public Mono<ServerResponse> consultarOrdenSalidaDevolucion(
        @Parameter(description = "Request con parámetros de consulta y paginación")
        ServerRequest request) {
        
        log.info("🔍 Iniciando consulta paginada de órdenes de salida por devolución");
        
        return extraerFiltrosConPaginacion(request)
            .flatMap(filtros -> {
                log.debug("📋 Filtros y paginación aplicados: {}", filtros);
                
                return consultarOrdenSalidaDevolucionUseCase.consultarOrdenSalidaDevolucion(filtros)
                    .flatMap(response -> {
                        log.info("✅ Consulta paginada completada. {} elementos en página {}/{}",
                                response.getPagination().getNumberOfElements(),
                                response.getPagination().getPage() + 1,
                                response.getPagination().getTotalPages());
                        return ServerResponse.ok()
                            .bodyValue(response);
                    });
            })
            .doOnError(error -> log.error("❌ Error al consultar órdenes de salida por devolución: {}", error.getMessage()))
            .onErrorResume(throwable -> {
                log.error("❌ Error no controlado al consultar órdenes de salida por devolución", throwable);
                return ServerResponse.status(500)
                    .bodyValue("Error interno del servidor: " + throwable.getMessage());
            });
    }
    
    /**
     * Extrae los filtros y parámetros de paginación del request y los convierte a un objeto de solicitud.
     * 
     * @param request el request del servidor
     * @return Mono con los filtros y paginación extraídos
     */
    private Mono<ListadoOrdenSalidaDevolucionRequest> extraerFiltrosConPaginacion(ServerRequest request) {
        return Mono.fromCallable(() -> {
            ListadoOrdenSalidaDevolucionRequest.ListadoOrdenSalidaDevolucionRequestBuilder builder = 
                ListadoOrdenSalidaDevolucionRequest.builder();
            
            // Extraer parámetros opcionales de filtros
            request.queryParam("nombreCliente")
                .ifPresent(builder::nombreCliente);
            
            request.queryParam("codigoSalida")
                .ifPresent(builder::codigoSalida);
            
            request.queryParam("numeroGuia")
                .ifPresent(builder::numeroGuia);
            
            // Extraer y validar fechas
            request.queryParam("fechaInicio")
                .ifPresent(fechaStr -> {
                    try {
                        LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
                        builder.fechaInicio(fecha);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Formato de fecha inválido para fechaInicio: " + fechaStr + ". Use formato yyyy-MM-dd");
                    }
                });
            
            request.queryParam("fechaFin")
                .ifPresent(fechaStr -> {
                    try {
                        LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
                        builder.fechaFin(fecha);
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Formato de fecha inválido para fechaFin: " + fechaStr + ". Use formato yyyy-MM-dd");
                    }
                });
            
            // Extraer parámetros de paginación
            var paginationBuilder = PaginationDTO.builder();
            
            request.queryParam("page")
                .ifPresent(pageStr -> {
                    try {
                        int page = Integer.parseInt(pageStr);
                        if (page < 0) {
                            throw new IllegalArgumentException("El número de página debe ser mayor o igual a 0");
                        }
                        paginationBuilder.page(page);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Formato de página inválido: " + pageStr);
                    }
                });
            
            request.queryParam("size")
                .ifPresent(sizeStr -> {
                    try {
                        int size = Integer.parseInt(sizeStr);
                        if (size <= 0 || size > 100) {
                            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
                        }
                        paginationBuilder.size(size);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Formato de tamaño inválido: " + sizeStr);
                    }
                });
            
            request.queryParam("sortBy")
                .ifPresent(paginationBuilder::sortBy);
            
            request.queryParam("sortDirection")
                .ifPresent(dir -> paginationBuilder.sortDirection(dir.toUpperCase()));
            
            builder.pagination(paginationBuilder.build());
            
            return builder.build();
        })
        .onErrorMap(throwable -> {
            log.error("❌ Error al extraer filtros y paginación: {}", throwable.getMessage());
            return throwable;
        });
    }
} 