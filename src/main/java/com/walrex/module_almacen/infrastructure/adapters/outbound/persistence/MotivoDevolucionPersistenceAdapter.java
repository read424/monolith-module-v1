package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.MotivoDevolucionPersistencePort;
import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.MotivoDevolucionMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.MotivoDevolucionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para motivos de devolución
 * Implementa el puerto de salida conectando el dominio con la infraestructura
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MotivoDevolucionPersistenceAdapter implements MotivoDevolucionPersistencePort {

    private final MotivoDevolucionRepository repository;
    private final MotivoDevolucionMapper mapper;

    @Override
    public Flux<MotivoDevolucionDTO> findAllActive() {
        log.debug("🔍 Adaptador: Obteniendo motivos activos");
        
        return repository.findAllActive()
                .map(mapper::toDTO)
                .doOnNext(motivo -> log.debug("✅ Adaptador: Motivo mapeado - ID: {}", motivo.getId()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al obtener motivos activos: {}", throwable.getMessage());
                    return new RuntimeException("Error de persistencia al obtener motivos activos", throwable);
                });
    }

    @Override
    public Flux<MotivoDevolucionDTO> findByDescripcionContains(String texto) {
        log.debug("🔍 Adaptador: Buscando motivos por descripción: '{}'", texto);
        
        return repository.findByDescripcionContains(texto)
                .map(mapper::toDTO)
                .doOnNext(motivo -> log.debug("✅ Adaptador: Motivo encontrado - ID: {}, Descripción: {}", 
                        motivo.getId(), motivo.getDescripcion()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al buscar motivos por descripción '{}': {}", texto, throwable.getMessage());
                    return new RuntimeException("Error de persistencia al buscar motivos", throwable);
                });
    }

    @Override
    public Mono<MotivoDevolucionDTO> save(MotivoDevolucionDTO motivoDevolucion) {
        log.debug("🔄 Adaptador: Guardando motivo de devolución: {}", motivoDevolucion.getDescripcion());
        
        return Mono.fromCallable(() -> mapper.toEntityForCreate(motivoDevolucion))
                .flatMap(repository::save)
                .map(mapper::toDTO)
                .doOnNext(motivoGuardado -> log.debug("✅ Adaptador: Motivo guardado - ID: {}, Descripción: {}", 
                        motivoGuardado.getId(), motivoGuardado.getDescripcion()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al guardar motivo '{}': {}", 
                            motivoDevolucion.getDescripcion(), throwable.getMessage());
                    return new RuntimeException("Error de persistencia al guardar motivo", throwable);
                });
    }

    @Override
    public Mono<MotivoDevolucionDTO> findById(Long id) {
        log.debug("🔍 Adaptador: Buscando motivo por ID: {}", id);
        
        return repository.findById(id)
                .map(mapper::toDTO)
                .doOnNext(motivo -> log.debug("✅ Adaptador: Motivo encontrado por ID {} - Descripción: {}", 
                        id, motivo.getDescripcion()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al buscar motivo por ID {}: {}", id, throwable.getMessage());
                    return new RuntimeException("Error de persistencia al buscar motivo por ID", throwable);
                });
    }

    @Override
    public Mono<MotivoDevolucionDTO> update(MotivoDevolucionDTO motivoDevolucion) {
        log.debug("🔄 Adaptador: Actualizando motivo ID: {} - {}", 
                motivoDevolucion.getId(), motivoDevolucion.getDescripcion());
        
        return Mono.fromCallable(() -> mapper.toEntityForUpdate(motivoDevolucion))
                .flatMap(repository::save)
                .map(mapper::toDTO)
                .doOnNext(motivoActualizado -> log.debug("✅ Adaptador: Motivo actualizado - ID: {}, Descripción: {}", 
                        motivoActualizado.getId(), motivoActualizado.getDescripcion()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al actualizar motivo ID {}: {}", 
                            motivoDevolucion.getId(), throwable.getMessage());
                    return new RuntimeException("Error de persistencia al actualizar motivo", throwable);
                });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.debug("🔄 Adaptador: Eliminando motivo ID: {}", id);
        
        return repository.deleteById(id)
                .doOnSuccess(v -> log.debug("✅ Adaptador: Motivo eliminado - ID: {}", id))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al eliminar motivo ID {}: {}", id, throwable.getMessage());
                    return new RuntimeException("Error de persistencia al eliminar motivo", throwable);
                });
    }

    @Override
    public Mono<Boolean> existsByDescripcion(String descripcion) {
        log.debug("🔍 Adaptador: Verificando existencia de descripción: '{}'", descripcion);
        
        return repository.findByDescripcionContains(descripcion)
                .filter(entity -> entity.getDescripcion().equalsIgnoreCase(descripcion))
                .hasElements()
                .doOnNext(existe -> log.debug("✅ Adaptador: Descripción '{}' existe: {}", descripcion, existe))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al verificar descripción '{}': {}", descripcion, throwable.getMessage());
                    return new RuntimeException("Error de persistencia al verificar descripción", throwable);
                });
    }

    @Override
    public Mono<Boolean> existsByDescripcionAndIdNot(String descripcion, Long idExcluido) {
        log.debug("🔍 Adaptador: Verificando existencia de descripción '{}' excluyendo ID: {}", descripcion, idExcluido);
        
        return repository.findByDescripcionContains(descripcion)
                .filter(entity -> entity.getDescripcion().equalsIgnoreCase(descripcion))
                .filter(entity -> !entity.getId().equals(idExcluido))
                .hasElements()
                .doOnNext(existe -> log.debug("✅ Adaptador: Descripción '{}' existe (excluyendo ID {}): {}", 
                        descripcion, idExcluido, existe))
                .onErrorMap(throwable -> {
                    log.error("❌ Error en adaptador al verificar descripción '{}' excluyendo ID {}: {}", 
                            descripcion, idExcluido, throwable.getMessage());
                    return new RuntimeException("Error de persistencia al verificar descripción", throwable);
                });
    }
} 