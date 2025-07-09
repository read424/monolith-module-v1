package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para gestionar motivos de devolución
 */
public interface GestionarMotivosDevolucionUseCase {

    /**
     * Obtiene todos los motivos de devolución activos
     * 
     * @return Flux de motivos activos ordenados por descripción
     */
    Flux<MotivoDevolucionDTO> obtenerMotivosActivos();

    /**
     * Busca motivos de devolución por descripción
     * 
     * @param texto texto a buscar en la descripción (case insensitive)
     * @return Flux de motivos que contienen el texto
     */
    Flux<MotivoDevolucionDTO> buscarMotivosPorDescripcion(String texto);

    /**
     * Crea un nuevo motivo de devolución
     * 
     * @param motivoDevolucion datos del motivo a crear
     * @return Mono con el motivo creado (incluyendo ID generado)
     */
    Mono<MotivoDevolucionDTO> crearMotivoDevolucion(MotivoDevolucionDTO motivoDevolucion);

    /**
     * Obtiene un motivo de devolución por su ID
     * 
     * @param id identificador del motivo
     * @return Mono con el motivo encontrado
     */
    Mono<MotivoDevolucionDTO> obtenerMotivoPorId(Long id);

    /**
     * Actualiza un motivo de devolución existente
     * 
     * @param id identificador del motivo a actualizar
     * @param motivoDevolucion datos actualizados del motivo
     * @return Mono con el motivo actualizado
     */
    Mono<MotivoDevolucionDTO> actualizarMotivoDevolucion(Long id, MotivoDevolucionDTO motivoDevolucion);

    /**
     * Desactiva un motivo de devolución (soft delete)
     * 
     * @param id identificador del motivo a desactivar
     * @return Mono que completa cuando el motivo ha sido desactivado
     */
    Mono<Void> desactivarMotivoDevolucion(Long id);
} 