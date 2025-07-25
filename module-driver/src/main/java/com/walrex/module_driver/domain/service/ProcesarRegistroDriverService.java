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
                                buscarConductorModel.getTipoDocumento().getIdTipoDocumento());

                return validarParametros(buscarConductorModel
                                .getNumeroDocumento(),
                                buscarConductorModel.getTipoDocumento()
                                                .getIdTipoDocumento())
                                .thenMany(conductorPersistencePort.buscarConductorPorDocumento(buscarConductorModel
                                                .getNumeroDocumento(),
                                                buscarConductorModel.getTipoDocumento()
                                                                .getIdTipoDocumento()))
                                .doOnNext(conductor -> log.info("✅ Conductor encontrado: {} {}",
                                                conductor.getNombres(), conductor.getApellidos()))
                                .doOnError(error -> log.error("❌ Error al buscar conductor: {}", error.getMessage()))
                                .switchIfEmpty(Flux.defer(() -> {
                                        log.info("⚠️  No se encontró conductor para Documento: {}, Tipo: {}",
                                                        buscarConductorModel.getNumeroDocumento(),
                                                        buscarConductorModel.getTipoDocumento().getIdTipoDocumento());
                                        return Flux.just(
                                                        ConductorDataDTO.builder()
                                                                        .tipoDocumento(TipoDocumentoDTO.builder()
                                                                                        .idTipoDocumento(
                                                                                                        buscarConductorModel
                                                                                                                        .getTipoDocumento()
                                                                                                                        .getIdTipoDocumento())
                                                                                        .build())
                                                                        .numeroDocumento(buscarConductorModel
                                                                                        .getNumeroDocumento())
                                                                        .build());
                                }));
        }

        /**
         * Valida los parámetros de entrada para la búsqueda.
         */
        private Mono<Void> validarParametros(String numDoc, Integer idTipDoc) {
                return Mono.fromRunnable(() -> {
                        if (numDoc == null || numDoc.trim().isEmpty()) {
                                throw new IllegalArgumentException("El número de documento no puede estar vacío");
                        }

                        if (idTipDoc == null) {
                                throw new IllegalArgumentException("El ID del tipo de documento no puede estar vacío");
                        }

                        log.debug("✅ Validación de parámetros completada");
                });
        }
}
