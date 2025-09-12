package com.walrex.module_driver.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_driver.application.ports.input.DriverCommandUseCase;
import com.walrex.module_driver.application.ports.output.ConductorPersistencePort;
import com.walrex.module_driver.application.ports.output.DriverPersistencePort;
import com.walrex.module_driver.domain.model.BuscarConductorModel;
import com.walrex.module_driver.domain.model.DriverDomain;
import com.walrex.module_driver.domain.model.dto.*;
import com.walrex.module_driver.domain.model.mapper.DriverDomainMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarRegistroDriverService implements DriverCommandUseCase {
        private final DriverDomainMapper driverDomainMapper;
        private final DriverPersistencePort driverPersistencePort;
        private final ConductorPersistencePort conductorPersistencePort;

        @Override
        public Mono<CreateDriverDTO> crear_conductor(DriverDomain driver, Integer idUsuario) {
                CreateDriverDTO createDriverDTO = driverDomainMapper.toDTO(driver);
                createDriverDTO.setIdUsuario(idUsuario);
                log.info("🛠️ Mapeo a CreateDriverDTO exitoso: {}", createDriverDTO);
                return driverPersistencePort.guardar_conductor(createDriverDTO)
                                .doOnNext(saved -> log.info("Conductor registrado exitosamente en dominio: {}", saved))
                                .onErrorResume(IllegalArgumentException.class, e -> {
                                        log.warn("⚠️ Error de validación en dominio: {}", e.getMessage());
                                        return Mono.error(new IllegalArgumentException(e.getMessage()));
                                })
                                .doOnError(e -> log.error("Error al registrar conductor en dominio: {}", e.getMessage(),
                                                e));
        }

        @Override
        public Mono<CreateDriverDTO> actualizar_conductor(Integer id, DriverDomain driver, Integer idUsuario) {
                CreateDriverDTO createDriverDTO = driverDomainMapper.toDTO(driver);
                createDriverDTO.setIdDriver(id);
                createDriverDTO.setIdUsuario(idUsuario);
                return driverPersistencePort.actualizar_datos_conductor(createDriverDTO)
                                .doOnNext(saved -> log.info("Conductor actualizado exitosamente en dominio: {}", saved))
                                .doOnError(e -> log.error("Error al actualizar conductor en dominio: {}",
                                                e.getMessage(), e));
        }

        @Override
        public Mono<Void> deshabilitar_conductor(Integer id, Integer idUsuario) {
                return driverPersistencePort.disabled_conductor(id)
                                .doOnSuccess(result -> log.info(
                                                "Conductor deshabilitado exitosamente en dominio, ID: {}, Usuario: {}",
                                                id, idUsuario))
                                .doOnError(e -> log.error("Error al deshabilitar conductor en dominio, ID: {}", id, e));
        }

        @Override
        public Mono<CreateDriverDTO> obtener_conductor_por_id(Integer id) {
                return driverPersistencePort.obtener_conductor_por_id(id)
                                .doOnNext(driver -> log.info("Conductor obtenido exitosamente en dominio, ID: {}", id))
                                .doOnError(e -> log.error("Error al obtener conductor en dominio, ID: {}", id, e));
        }

        @Override
        public Flux<ConductorDataDTO> buscarDatosDeConductorByNumDocAndIdTipDoc(
                        BuscarConductorModel buscarConductorModel) {
                log.info("🔍 Iniciando búsqueda de conductor - Documento: {}, Tipo: {}",
                                buscarConductorModel.getNumeroDocumento(),
                                buscarConductorModel.getTipoDocumento() != null ? 
                                        buscarConductorModel.getTipoDocumento().getIdTipoDocumento() : "null");

                return validarParametrosCompletos(buscarConductorModel)
                                .thenMany(conductorPersistencePort.buscarConductorPorDocumento(buscarConductorModel
                                                .getNumeroDocumento(),
                                                buscarConductorModel.getTipoDocumento()
                                                                .getIdTipoDocumento()))
                                .doOnNext(conductor -> log.info("✅ Conductor encontrado: {} {}",
                                                conductor.getNombres(), conductor.getApellidos()))
                                .doOnError(error -> log.error("❌ Error al buscar conductor: {}", error.getMessage()))
                                .doOnComplete(() -> {
                                        log.info("⚠️  No se encontró conductor para Documento: {}, Tipo: {}",
                                                        buscarConductorModel.getNumeroDocumento(),
                                                        buscarConductorModel.getTipoDocumento().getIdTipoDocumento());
                                });
        }

        @Override
        public Flux<ConductorDataDTO> buscarConductorPorParametros(SearchDriverByParameters searchDriverByParameters) {
                log.info("🔍 Iniciando búsqueda avanzada de conductor - Parámetros: {}", searchDriverByParameters);

                return validarParametrosAvanzados(searchDriverByParameters)
                        .thenMany(conductorPersistencePort.buscarConductorPorParametros(searchDriverByParameters))
                        .doOnNext(conductor -> log.info("✅ Conductor encontrado: {} {}",
                                        conductor.getNombres(), conductor.getApellidos()))
                        .doOnError(error -> log.error("❌ Error al buscar conductor: {}", error.getMessage()))
                        .switchIfEmpty(Flux.defer(() -> {
                                log.info("⚠️  No se encontró conductor para los parámetros: {}", searchDriverByParameters);
                                return Flux.empty();
                        }));
        }

        /**
         * Valida el modelo completo de búsqueda de conductor.
         */
        private Mono<Void> validarParametrosCompletos(BuscarConductorModel buscarConductorModel) {
                return Mono.defer(() -> {
                        if (buscarConductorModel == null) {
                                return Mono.error(new IllegalArgumentException("El modelo de búsqueda no puede estar vacío"));
                        }

                        if (buscarConductorModel.getNumeroDocumento() == null || buscarConductorModel.getNumeroDocumento().trim().isEmpty()) {
                                return Mono.error(new IllegalArgumentException("El número de documento no puede estar vacío"));
                        }

                        if (buscarConductorModel.getTipoDocumento() == null) {
                                return Mono.error(new IllegalArgumentException("El tipo de documento no puede estar vacío"));
                        }

                        if (buscarConductorModel.getTipoDocumento().getIdTipoDocumento() == null) {
                                return Mono.error(new IllegalArgumentException("El ID del tipo de documento no puede estar vacío"));
                        }

                        log.debug("✅ Validación completa de parámetros completada");
                        return Mono.empty();
                });
        }


        /**
         * Valida los parámetros de entrada para la búsqueda avanzada.
         */
        private Mono<Void> validarParametrosAvanzados(SearchDriverByParameters searchDriverByParameters) {
            return Mono.defer(() -> {
                    if (searchDriverByParameters == null) {
                            return Mono.error(new IllegalArgumentException("Los parámetros de búsqueda no pueden estar vacíos"));
                    }

                    // Validar que al menos un parámetro esté presente
                    boolean hasValidParam = false;

                    if (searchDriverByParameters.getNumDoc() != null && !searchDriverByParameters.getNumDoc().trim().isEmpty()) {
                            hasValidParam = true;
                    }

                    if (searchDriverByParameters.getIdTipDoc() != null && !searchDriverByParameters.getIdTipDoc().equals(0)) {
                            hasValidParam = true;
                    }

                    if (searchDriverByParameters.getName() != null && !searchDriverByParameters.getName().trim().isEmpty()) {
                            hasValidParam = true;
                    }

                    if (!hasValidParam) {
                            return Mono.error(new IllegalArgumentException("Al menos un parámetro de búsqueda debe estar presente"));
                    }

                    log.debug("✅ Validación de parámetros avanzados completada");
                    return Mono.empty();
            });
        }
}
