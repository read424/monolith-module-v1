package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence;

import com.walrex.module_comercial.application.ports.output.SolicitudCambioPersistencePort;
import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioResponseDTO;
import com.walrex.module_comercial.domain.dto.PartidaInfo;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.SolicitudCambioServicioPartidaDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.SolicitudPartidaAdicionalDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity.SolicitudCambioServicioPartidaEntity;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity.SolicitudPartidaAdicionalEntity;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.repository.SolicitudCambioServicioPartidaR2dbcRepository;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.repository.SolicitudPartidaAdicionalR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Adapter de persistencia para solicitudes de cambio de servicio.
 * Implementa el puerto de salida {@link SolicitudCambioPersistencePort}.
 *
 * Responsabilidad: Traducir operaciones de dominio a operaciones de persistencia R2DBC.
 * Coordina el guardado de solicitudes principales y partidas adicionales de forma reactiva.
 *
 * @author Sistema
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CambioServicioPartidaPersistenceAdapter implements SolicitudCambioPersistencePort {

    private final SolicitudCambioServicioPartidaR2dbcRepository solicitudRepository;
    private final SolicitudPartidaAdicionalR2dbcRepository partidaAdicionalRepository;

    @Override
    public Mono<GuardarSolicitudCambioResponseDTO> guardarSolicitudCambio(SolicitudCambioServicioPartidaDTO solicitudDTO) {
        log.info("💾 [PERSISTENCE] Iniciando guardado de solicitud de cambio para partida: {}", solicitudDTO.getIdPartida());
        log.info("📊 [PERSISTENCE] DTO recibido - partidasAdicionales: {}, cnt: {}",
                solicitudDTO.getPartidasAdicionales(),
                solicitudDTO.getPartidasAdicionalesDTO() != null ? solicitudDTO.getPartidasAdicionalesDTO().size() : 0);

        return guardarSolicitudPrincipal(solicitudDTO)
                .doOnSubscribe(sub -> log.info("📡 [PERSISTENCE] Suscripción a guardarSolicitudPrincipal iniciada"))
                .flatMap(solicitudGuardada -> {
                    log.info("✅ [PERSISTENCE] Solicitud principal guardada con ID: {}", solicitudGuardada.getId());

                    // Si hay partidas adicionales en el DTO, guardarlas
                    if (solicitudDTO.getPartidasAdicionalesDTO() != null && !solicitudDTO.getPartidasAdicionalesDTO().isEmpty()) {
                        log.info("🔄 [PERSISTENCE] Guardando {} partidas adicionales", solicitudDTO.getPartidasAdicionalesDTO().size());
                        return guardarPartidasAdicionales(solicitudGuardada.getId(), solicitudDTO.getPartidasAdicionalesDTO())
                                .collectList()
                                .flatMap(partidasGuardadas -> {
                                    log.info("✅ [PERSISTENCE] {} partidas adicionales guardadas exitosamente", partidasGuardadas.size());
                                    return construirResponse(solicitudGuardada, partidasGuardadas);
                                });
                    } else {
                        log.info("ℹ️ [PERSISTENCE] No hay partidas adicionales que guardar");
                        return construirResponse(solicitudGuardada, List.of());
                    }
                })
                .doOnSuccess(response -> log.info("✅ [PERSISTENCE] Response construido exitosamente"))
                .doOnError(error -> log.error("❌ [PERSISTENCE] Error al guardar solicitud de cambio: {}", error.getMessage(), error));
    }

    /**
     * Guarda la solicitud principal de cambio de servicio.
     * Mapea el DTO enriquecido a la entidad de persistencia.
     *
     * @param solicitudDTO DTO enriquecido con valores OLD y NEW de la BD
     * @return Mono con la entidad guardada
     */
    private Mono<SolicitudCambioServicioPartidaEntity> guardarSolicitudPrincipal(SolicitudCambioServicioPartidaDTO solicitudDTO) {
        log.debug("📝 Construyendo entidad de solicitud principal desde DTO enriquecido");
        log.debug("   - Valores OLD vienen de BD actual");
        log.debug("   - Valores NEW vienen del request del usuario");

        SolicitudCambioServicioPartidaEntity entity = SolicitudCambioServicioPartidaEntity.builder()
                // Datos básicos
                .idPartida(solicitudDTO.getIdPartida())

                // Valores OLD (estado actual de la BD)
                .idOrdenproduccionOld(solicitudDTO.getIdOrdenproduccionOld())
                .idOrdenOld(solicitudDTO.getIdOrdenOld())
                .idDetOsOld(solicitudDTO.getIdDetOsOld())
                .idPrecioOld(solicitudDTO.getIdPrecioOld())
                .precioOld(solicitudDTO.getPrecioOld())
                .idGamaOld(solicitudDTO.getIdGamaOld())
                .idRutaOld(solicitudDTO.getIdRutaOld())
                .descArticuloOld(solicitudDTO.getDescArticuloOld())

                // Valores NEW (propuestos por el usuario)
                .idOrdenproduccion(solicitudDTO.getIdOrdenproduccion())
                .idOrden(solicitudDTO.getIdOrden())
                .idDetOs(solicitudDTO.getIdDetOs())
                .idRuta(solicitudDTO.getIdRuta())
                .idGama(solicitudDTO.getIdGama())
                .idPrecio(solicitudDTO.getIdPrecio())
                .precio(solicitudDTO.getPrecio())

                // Estado y control
                .status(solicitudDTO.getStatus())
                .aprobado(solicitudDTO.getAprobado())
                .porAprobar(solicitudDTO.getPorAprobar())
                .partidasAdicionales(solicitudDTO.getPartidasAdicionales())

                // Usuario
                .idUsuario(solicitudDTO.getIdUsuario())
                .idUsuarioAutorizado(solicitudDTO.getIdUsuarioAutorizado())

                // Auditoría
                .fecRegistro(solicitudDTO.getFecRegistro())
                .build();

        return solicitudRepository.save(entity)
                .doOnNext(saved -> log.debug("💾 Solicitud principal guardada: ID={}", saved.getId()));
    }

    /**
     * Guarda las partidas adicionales relacionadas con la solicitud.
     * Mapea cada DTO de partida adicional a su entidad correspondiente.
     *
     * @param idSolicitud ID de la solicitud principal
     * @param partidasDTO Lista de DTOs con los datos de partidas adicionales
     * @return Flux con las entidades guardadas
     */
    private Flux<SolicitudPartidaAdicionalEntity> guardarPartidasAdicionales(
            Integer idSolicitud,
            List<SolicitudPartidaAdicionalDTO> partidasDTO) {

        if (partidasDTO == null || partidasDTO.isEmpty()) {
            log.debug("ℹ️ No hay partidas adicionales que guardar");
            return Flux.empty();
        }

        log.debug("📝 Guardando {} partidas adicionales para solicitud ID: {}", partidasDTO.size(), idSolicitud);

        return Flux.fromIterable(partidasDTO)
                .flatMap(partidaDTO -> {
                    log.debug("   - Mapeando partida adicional: idPartida={}, codPartida={}",
                            partidaDTO.getIdPartida(), partidaDTO.getCodPartida());

                    SolicitudPartidaAdicionalEntity entity = SolicitudPartidaAdicionalEntity.builder()
                            .idSolicitud(idSolicitud) // FK a la solicitud principal
                            .idPartida(partidaDTO.getIdPartida())
                            .status(partidaDTO.getStatus())
                            .aprobado(partidaDTO.getAprobado())
                            .createAt(partidaDTO.getCreateAt())
                            .updateAt(partidaDTO.getUpdateAt())
                            .build();

                    return partidaAdicionalRepository.save(entity);
                })
                .doOnNext(saved -> log.debug("💾 Partida adicional guardada: ID={}, idPartida={}",
                        saved.getId(), saved.getIdPartida()));
    }

    /**
     * Construye el DTO de respuesta a partir de la solicitud guardada y sus partidas adicionales.
     *
     * @param solicitud Entidad de solicitud principal guardada
     * @param partidasAdicionales Lista de partidas adicionales guardadas
     * @return Mono con el DTO de respuesta
     */
    private Mono<GuardarSolicitudCambioResponseDTO> construirResponse(
            SolicitudCambioServicioPartidaEntity solicitud,
            List<SolicitudPartidaAdicionalEntity> partidasAdicionales) {

        log.debug("📦 Construyendo response DTO");

        // Convertir partidas adicionales a PartidaInfo
        List<PartidaInfo> partidasInfo = partidasAdicionales.stream()
                .map(partida -> new PartidaInfo(
                        null,
                        partida.getIdPartida(),
                        partida.getStatus()
                ))
                .toList();

        GuardarSolicitudCambioResponseDTO response = new GuardarSolicitudCambioResponseDTO(
                "OP-" + solicitud.getIdOrdenproduccion(), // TODO: Obtener código real
                solicitud.getIdOrdenproduccion(),
                solicitud.getIdRuta(),
                true, // isProcess - siempre true después de guardar
                solicitud.getPorAprobar() == 1, // pendingApproval
                partidasInfo
        );

        return Mono.just(response);
    }
}
