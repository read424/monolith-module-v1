package com.walrex.module_driver.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_driver.application.ports.output.DriverPersistencePort;
import com.walrex.module_driver.domain.model.dto.CreateDriverDTO;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity.DriverEntity;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.mapper.DriverEntityMapper;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.repository.DriverRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriverPersistenceAdapter implements DriverPersistencePort {
    private final DriverRepository driverRepository;
    private final DriverEntityMapper driverEntityMapper;

    @Override
    public Mono<CreateDriverDTO> guardar_conductor(CreateDriverDTO driver) {
        log.info("🛠️ Validando documento en repository: {}", driver);
        return validarDocumentoNoExiste(driver)
                .flatMap(noExiste -> {
                    if (!noExiste) {
                        return Mono.error(new IllegalArgumentException(
                                "El documento de identidad ya existe para otro conductor."));
                    }
                    DriverEntity driverEntity = driverEntityMapper.toEntity(driver);
                    log.info("🛠️ Mapeo a DriverEntity exitoso: {}", driverEntity);
                    return driverRepository.save(driverEntity)
                            .map(driverEntityMapper::toDTO)
                            .onErrorResume(IllegalArgumentException.class, e -> {
                                log.warn("⚠️ Error de validación al guardar DriverEntity: {}", e.getMessage());
                                return Mono.error(new IllegalArgumentException(e.getMessage()));
                            })
                            .doOnNext(saved -> log.info("Conductor guardado exitosamente: {}", saved))
                            .doOnError(e -> log.error("Error al guardar conductor: {}", e.getMessage(), e));
                });
    }

    private Mono<Boolean> validarDocumentoNoExiste(CreateDriverDTO driver) {
        Long idDriver = driver.getIdDriver() != null ? Long.valueOf(driver.getIdDriver()) : 0L;
        log.info("🔍 Iniciando validación con idDriver: {}", idDriver);

        return driverRepository.ValidDocumentNotExists(driver.getIdTipoDocumento(), driver.getNumDocumento(), idDriver)
                .doOnSubscribe(sub -> log.info("✅ Suscrito a ValidDocumentNotExists"))
                .doOnNext(existingDriver -> log.info("📋 Resultado de ValidDocumentNotExists: {}", existingDriver))
                .onErrorResume(e -> {
                    log.error("❌ Error al validar documento en repository: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("Error 505: No se pudo validar el documento del conductor"));
                })
                .map(existingDriver -> {
                    log.info("🛠️ existingDriver en map: {}", existingDriver);
                    boolean noExiste = existingDriver == null;
                    log.info("🛠️ Resultado final de validación (noExiste): {}", noExiste);
                    return noExiste;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("🔄 ValidDocumentNotExists retornó empty, documento no existe");
                    return Mono.just(true);
                }))
                .doOnSuccess(result -> log.info("✅ Validación completada exitosamente: {}", result))
                .doOnError(e -> log.error("❌ Error en validación: {}", e.getMessage(), e))
                .doFinally(signal -> log.info("🏁 Finalizó validación con señal: {}", signal));
    }

    @Override
    public Mono<CreateDriverDTO> actualizar_datos_conductor(CreateDriverDTO driverDto) {
        return driverRepository
                .ValidDocumentNotExists(driverDto.getIdTipoDocumento(), driverDto.getNumDocumento(),
                        Long.valueOf(driverDto.getIdDriver()))
                .flatMap(existingDriver -> {
                    if (existingDriver != null) {
                        return Mono.error(new IllegalArgumentException(
                                "El documento de identidad ya existe para otro conductor."));
                    }
                    DriverEntity driverEntity = driverEntityMapper.toEntity(driverDto);
                    return driverRepository.save(driverEntity)
                            .map(driverEntityMapper::toDTO)
                            .doOnNext(saved -> log.info("Conductor actualizado exitosamente: {}", saved))
                            .doOnError(e -> log.error("Error al actualizar conductor: {}", e.getMessage(), e));
                });
    }

    @Override
    public Mono<Void> disabled_conductor(Integer id) {
        return driverRepository.disabledByIdLogical(Long.valueOf(id))
                .doOnSuccess(result -> log.info("Conductor eliminado lógicamente con éxito, ID: {}", id))
                .doOnError(e -> log.error("Error al eliminar conductor, ID: {}", id, e));
    }

    @Override
    public Mono<CreateDriverDTO> obtener_conductor_por_id(Integer id) {
        return driverRepository.findByIdAndStatusActive(Long.valueOf(id))
                .map(driverEntityMapper::toDTO)
                .doOnNext(driver -> log.info("Conductor obtenido exitosamente, ID: {}", id))
                .doOnError(e -> log.error("Error al obtener conductor, ID: {}", id, e));
    }

}
