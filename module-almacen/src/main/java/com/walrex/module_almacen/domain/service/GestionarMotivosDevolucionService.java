package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.GestionarMotivosDevolucionUseCase;
import com.walrex.module_almacen.application.ports.output.MotivoDevolucionPersistencePort;
import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para gestionar motivos de devolución
 * Implementa las reglas de negocio para los motivos de devolución
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GestionarMotivosDevolucionService implements GestionarMotivosDevolucionUseCase {

    private final MotivoDevolucionPersistencePort motivoDevolucionPersistencePort;

    @Override
    public Flux<MotivoDevolucionDTO> obtenerMotivosActivos() {
        log.debug("🔍 Obteniendo motivos de devolución activos");
        
        return motivoDevolucionPersistencePort.findAllActive()
                .doOnNext(motivo -> log.debug("✅ Motivo obtenido: {} - {}", motivo.getId(), motivo.getDescripcion()))
                .doOnComplete(() -> log.debug("✅ Consulta de motivos activos completada"));
    }

    @Override
    public Flux<MotivoDevolucionDTO> buscarMotivosPorDescripcion(String texto) {
        log.debug("🔍 Buscando motivos por descripción: '{}'", texto);
        
        if (texto == null || texto.trim().isEmpty()) {
            log.debug("⚠️ Texto de búsqueda vacío, retornando motivos activos");
            return obtenerMotivosActivos();
        }
        
        return motivoDevolucionPersistencePort.findByDescripcionContains(texto.trim())
                .doOnNext(motivo -> log.debug("✅ Motivo encontrado: {} - {}", motivo.getId(), motivo.getDescripcion()))
                .doOnComplete(() -> log.debug("✅ Búsqueda por descripción '{}' completada", texto));
    }

    @Override
    public Mono<MotivoDevolucionDTO> crearMotivoDevolucion(MotivoDevolucionDTO motivoDevolucion) {
        log.debug("🔄 Creando nuevo motivo de devolución: {}", motivoDevolucion.getDescripcion());
        
        return validarMotivoParaCreacion(motivoDevolucion)
                .then(motivoDevolucionPersistencePort.save(motivoDevolucion))
                .doOnNext(motivoCreado -> log.debug("✅ Motivo de devolución creado - ID: {}, Descripción: {}", 
                        motivoCreado.getId(), motivoCreado.getDescripcion()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error al crear motivo de devolución: {}", throwable.getMessage());
                    return new RuntimeException("Error al crear motivo de devolución: " + throwable.getMessage(), throwable);
                });
    }

    @Override
    public Mono<MotivoDevolucionDTO> obtenerMotivoPorId(Long id) {
        log.debug("🔍 Obteniendo motivo de devolución por ID: {}", id);
        
        if (id == null || id <= 0) {
            return Mono.error(new IllegalArgumentException("ID del motivo debe ser válido"));
        }
        
        return motivoDevolucionPersistencePort.findById(id)
                .doOnNext(motivo -> log.debug("✅ Motivo encontrado: {} - {}", motivo.getId(), motivo.getDescripcion()))
                .switchIfEmpty(Mono.error(new RuntimeException("Motivo de devolución no encontrado con ID: " + id)));
    }

    @Override
    public Mono<MotivoDevolucionDTO> actualizarMotivoDevolucion(Long id, MotivoDevolucionDTO motivoDevolucion) {
        log.debug("🔄 Actualizando motivo de devolución ID: {} con datos: {}", id, motivoDevolucion.getDescripcion());
        
        return obtenerMotivoPorId(id)
                .then(validarMotivoParaActualizacion(motivoDevolucion))
                .then(Mono.defer(() -> {
                    motivoDevolucion.setId(id);
                    return motivoDevolucionPersistencePort.update(motivoDevolucion);
                }))
                .doOnNext(motivoActualizado -> log.debug("✅ Motivo de devolución actualizado - ID: {}, Descripción: {}", 
                        motivoActualizado.getId(), motivoActualizado.getDescripcion()))
                .onErrorMap(throwable -> {
                    log.error("❌ Error al actualizar motivo de devolución ID {}: {}", id, throwable.getMessage());
                    return new RuntimeException("Error al actualizar motivo de devolución: " + throwable.getMessage(), throwable);
                });
    }

    @Override
    public Mono<Void> desactivarMotivoDevolucion(Long id) {
        log.debug("🔄 Desactivando motivo de devolución ID: {}", id);
        
        return obtenerMotivoPorId(id)
                .flatMap(motivo -> {
                    motivo.setStatus(0); // 0 = inactivo
                    return motivoDevolucionPersistencePort.update(motivo);
                })
                .doOnNext(motivoDesactivado -> log.debug("✅ Motivo de devolución desactivado - ID: {}", id))
                .then()
                .onErrorMap(throwable -> {
                    log.error("❌ Error al desactivar motivo de devolución ID {}: {}", id, throwable.getMessage());
                    return new RuntimeException("Error al desactivar motivo de devolución: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Valida los datos del motivo antes de la creación
     */
    private Mono<Void> validarMotivoParaCreacion(MotivoDevolucionDTO motivo) {
        return validarDescripcionMotivo(motivo)
                .then(Mono.defer(() -> {
                    // ✅ Asignar status activo por defecto
                    if (motivo.getStatus() == null) {
                        motivo.setStatus(1);
                    }
                    
                    return validarDescripcionUnica(motivo.getDescripcion());
                }));
    }

    /**
     * Valida los datos del motivo antes de la actualización
     */
    private Mono<Void> validarMotivoParaActualizacion(MotivoDevolucionDTO motivo) {
        return validarDescripcionMotivo(motivo)
                .then(validarDescripcionUnicaParaActualizacion(motivo.getDescripcion(), motivo.getId()));
    }

    /**
     * Valida y normaliza la descripción del motivo
     */
    private Mono<Void> validarDescripcionMotivo(MotivoDevolucionDTO motivo) {
        return Mono.defer(() -> {
            // ✅ Validar descripción obligatoria
            if (motivo.getDescripcion() == null || motivo.getDescripcion().trim().isEmpty()) {
                return Mono.error(new IllegalArgumentException("La descripción del motivo es obligatoria"));
            }
            
            // ✅ Validar longitud de descripción
            if (motivo.getDescripcion().trim().length() > 255) {
                return Mono.error(new IllegalArgumentException("La descripción no puede exceder 255 caracteres"));
            }
            
            // ✅ Normalizar descripción
            motivo.setDescripcion(motivo.getDescripcion().trim().toUpperCase());
            
            return Mono.empty();
        });
    }

    /**
     * Valida que la descripción sea única (para creación)
     */
    private Mono<Void> validarDescripcionUnica(String descripcion) {
        return motivoDevolucionPersistencePort.existsByDescripcion(descripcion)
                .flatMap(existe -> {
                    if (existe) {
                        return Mono.error(new IllegalArgumentException(
                                "Ya existe un motivo de devolución con la descripción: " + descripcion));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Valida que la descripción sea única excluyendo el ID actual (para actualización)
     * Implementa el patrón: buscar descripción donde ID sea diferente al actual
     */
    private Mono<Void> validarDescripcionUnicaParaActualizacion(String descripcion, Long idActual) {
        return motivoDevolucionPersistencePort.existsByDescripcionAndIdNot(descripcion, idActual)
                .flatMap(existe -> {
                    if (existe) {
                        return Mono.error(new IllegalArgumentException(
                                "Ya existe un motivo de devolución con la descripción: " + descripcion));
                    }
                    return Mono.empty();
                });
    }
} 