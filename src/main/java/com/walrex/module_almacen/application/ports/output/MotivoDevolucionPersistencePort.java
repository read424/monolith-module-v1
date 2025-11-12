package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de motivos de devolución
 */
public interface MotivoDevolucionPersistencePort {

    /**
     * Obtiene todos los motivos de devolución activos
     * 
     * @return Flux de motivos activos ordenados por descripción
     */
    Flux<MotivoDevolucionDTO> findAllActive();

    /**
     * Busca motivos de devolución por descripción
     * 
     * @param texto texto a buscar en la descripción
     * @return Flux de motivos que contienen el texto
     */
    Flux<MotivoDevolucionDTO> findByDescripcionContains(String texto);

    /**
     * Guarda un nuevo motivo de devolución
     * 
     * @param motivoDevolucion datos del motivo a guardar
     * @return Mono con el motivo guardado (incluyendo ID generado)
     */
    Mono<MotivoDevolucionDTO> save(MotivoDevolucionDTO motivoDevolucion);

    /**
     * Busca un motivo de devolución por su ID
     * 
     * @param id identificador del motivo
     * @return Mono con el motivo encontrado
     */
    Mono<MotivoDevolucionDTO> findById(Long id);

    /**
     * Actualiza un motivo de devolución existente
     * 
     * @param motivoDevolucion datos del motivo a actualizar
     * @return Mono con el motivo actualizado
     */
    Mono<MotivoDevolucionDTO> update(MotivoDevolucionDTO motivoDevolucion);

    /**
     * Elimina un motivo de devolución por su ID
     * 
     * @param id identificador del motivo a eliminar
     * @return Mono que completa cuando el motivo ha sido eliminado
     */
    Mono<Void> deleteById(Long id);

    /**
     * Verifica si existe un motivo con la descripción especificada
     * 
     * @param descripcion descripción a verificar
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsByDescripcion(String descripcion);

    /**
     * Verifica si existe un motivo con la descripción especificada excluyendo un ID específico
     * Útil para validar unicidad en actualizaciones
     * 
     * @param descripcion descripción a verificar
     * @param idExcluido ID a excluir de la búsqueda
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsByDescripcionAndIdNot(String descripcion, Long idExcluido);
} 