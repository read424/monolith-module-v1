package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.walrex.module_almacen.application.ports.input.GestionarMotivosDevolucionUseCase;
import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.CrearMotivoDevolucionRequestMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.MotivoDevolucionResponseMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.CrearMotivoDevolucionRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.MotivoDevolucionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Handler reactivo para motivos de devolución
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Motivos de Devolución", description = "Endpoints para gestionar motivos de devolución")
public class MotivosDevolucionHandler {

    private final GestionarMotivosDevolucionUseCase gestionarMotivosDevolucionUseCase;
    private final MotivoDevolucionResponseMapper responseMapper;
    private final CrearMotivoDevolucionRequestMapper requestMapper;

    /**
     * Obtener todos los motivos activos
     */
    @Operation(
        summary = "Obtener motivos de devolución activos",
        description = "Obtiene la lista completa de motivos de devolución que están activos en el sistema"
    )
    @ApiResponses(value = {
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
            description = "Error al obtener motivos de devolución",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public Mono<ServerResponse> obtenerMotivosActivos(ServerRequest request) {
        log.debug("🔍 Handler: Obteniendo motivos de devolución activos");
        
        return gestionarMotivosDevolucionUseCase.obtenerMotivosActivos()
                .collectList()
                .doOnNext(motivos -> log.debug("✅ Handler: Obtenidos {} motivos activos", motivos.size()))
                .map(responseMapper::toResponseList)
                .flatMap(motivos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(motivos))
                .onErrorResume(error -> {
                    log.error("❌ Handler: Error al obtener motivos de devolución: {}", error.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("Error al obtener motivos de devolución: " + error.getMessage());
                });
    }

    /**
     * Buscar motivos por descripción
     */
    @Operation(
        summary = "Buscar motivos por descripción",
        description = "Busca motivos de devolución que contengan el texto especificado en su descripción"
    )
    @ApiResponses(value = {
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
            description = "Error al buscar motivos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public Mono<ServerResponse> buscarMotivosPorDescripcion(
        @Parameter(description = "Texto a buscar en la descripción del motivo", example = "defecto")
        ServerRequest request) {
        String texto = request.queryParam("q").orElse("");
        log.debug("🔍 Handler: Buscando motivos por descripción: {}", texto);
        
        return gestionarMotivosDevolucionUseCase.buscarMotivosPorDescripcion(texto)
                .collectList()
                .doOnNext(motivos -> log.debug("✅ Handler: Encontrados {} motivos con descripción '{}'", motivos.size(), texto))
                .map(responseMapper::toResponseList)
                .flatMap(motivos -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(motivos))
                .onErrorResume(error -> {
                    log.error("❌ Handler: Error al buscar motivos por descripción '{}': {}", texto, error.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("Error al buscar motivos: " + error.getMessage());
                });
    }

    /**
     * Crear nuevo motivo de devolución
     */
    @Operation(
        summary = "Crear nuevo motivo de devolución",
        description = "Crea un nuevo motivo de devolución con la descripción especificada"
    )
    @ApiResponses(value = {
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
            description = "Error al crear motivo de devolución (datos inválidos o descripción duplicada)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    public Mono<ServerResponse> crearMotivoDevolucion(
        @Parameter(description = "Datos del motivo de devolución a crear")
        ServerRequest request) {
        log.debug("🔄 Handler: Creando nuevo motivo de devolución");
        
        return request.bodyToMono(CrearMotivoDevolucionRequest.class)
                .doOnNext(requestDto -> log.debug("🔄 Handler: Request recibido - Descripción: {}", requestDto.getDescripcion()))
                .map(requestMapper::toDTO)
                .doOnNext(motivoDto -> log.debug("🔄 Handler: DTO mapeado - Descripción: {}, Status: {}", 
                        motivoDto.getDescripcion(), motivoDto.getStatus()))
                .flatMap(gestionarMotivosDevolucionUseCase::crearMotivoDevolucion)
                .doOnNext(motivoCreado -> log.debug("✅ Handler: Motivo creado - ID: {}, Descripción: {}", 
                        motivoCreado.getId(), motivoCreado.getDescripcion()))
                .map(responseMapper::toResponse)
                .flatMap(motivoCreado -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(motivoCreado))
                .onErrorResume(error -> {
                    log.error("❌ Handler: Error al crear motivo de devolución: {}", error.getMessage());
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("Error al crear motivo de devolución: " + error.getMessage());
                });
    }
} 