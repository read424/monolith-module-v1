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
        // Suponiendo que los otros parámetros pueden ser null o valores por defecto
        return driverRepository.ValidDocumentNotExists(driver.getIdTipoDocumento(), driver.getNumDocumento(), null)
                .flatMap(existingDriver -> {
                    if (existingDriver != null) {
                        return Mono.error(new IllegalArgumentException(
                                "El documento de identidad ya existe para otro conductor."));
                    }
                    DriverEntity driverEntity = driverEntityMapper.toEntity(driver);
                    return driverRepository.save(driverEntity)
                            .map(driverEntityMapper::toDTO)
                            .doOnNext(saved -> log.info("Conductor guardado exitosamente: {}", saved))
                            .doOnError(e -> log.error("Error al guardar conductor: {}", e.getMessage(), e));
                });
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
