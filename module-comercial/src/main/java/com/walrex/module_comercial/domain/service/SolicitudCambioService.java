package com.walrex.module_comercial.domain.service;

import com.walrex.module_comercial.application.ports.input.GuardarSolicitudCambioServicioUseCase;
import com.walrex.module_comercial.application.ports.output.SolicitudCambioPersistencePort;
import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioRequestDTO;
import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioResponseDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.ComercialRepositoryAdapter;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.OrdenProduccionPartidaDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.SolicitudCambioServicioPartidaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para gestionar solicitudes de cambio de servicio.
 *
 * Responsabilidad: Coordinar la lógica de negocio, validar datos contra la BD
 * y delegar la persistencia al adapter correspondiente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudCambioService implements GuardarSolicitudCambioServicioUseCase {

    private final SolicitudCambioPersistencePort solicitudCambioPersistencePort;
    private final ComercialRepositoryAdapter comercialRepositoryAdapter;

    @Override
    public Mono<GuardarSolicitudCambioResponseDTO> guardarSolicitudCambioServicio(GuardarSolicitudCambioRequestDTO request) {
        log.info("🔄 Iniciando proceso de guardado de solicitud de cambio de servicio");
        log.info("📋 Request - idPartida: {}, aplicarOtrasPartidas: {}",
                request.getIdPartida(), request.getAplicarOtrasPartidas());

        return validarCamposObligatorios(request)
                .then(getOrdenProduccionPartida(request))
                .doOnNext(dto -> log.info("📦 DTO enriquecido creado para partida: {}", dto.getIdPartida()))
                .flatMap(requestEnriquecido -> {
                    log.info("💾 Iniciando guardado en persistencia para partida: {}", requestEnriquecido.getIdPartida());
                    return solicitudCambioPersistencePort.guardarSolicitudCambio(requestEnriquecido);
                })
                .doOnSuccess(response -> log.info("✅ Solicitud de cambio guardada exitosamente para partida: {}",
                        request.getIdPartida()))
                .doOnError(error -> log.error("❌ Error al guardar solicitud de cambio: {}", error.getMessage(), error));
    }

    /**
     * Valida que los campos obligatorios del request no sean nulos.
     * Primera capa de validación: estructura del request.
     */
    private Mono<Void> validarCamposObligatorios(GuardarSolicitudCambioRequestDTO request) {
        return Mono.defer(() -> {
            log.debug("🔍 Validando campos obligatorios del request");

            if ((request.getSolicitudCambio().getIdRuta() == null && request.getSolicitudCambio().getIdGama() == null && request.getSolicitudCambio().getIdPrecio()==null) || request.getSolicitudCambio().getIdOrdenproduccion()==null) {
                log.error("❌ Validación fallida: idPartida es obligatorio");
                return Mono.error(new IllegalArgumentException("Faltan Datos para procesar el cambio de Servicio"));
            }
            return Mono.empty();
        });
    }

    /**
     * Enriquece el request con datos reales de BD y construye el DTO de persistencia.
     *
     * IMPORTANTE: Este método construye el SolicitudCambioServicioPartidaDTO que contiene:
     * - Valores OLD: Obtenidos de la BD (estado actual)
     * - Valores NEW: Obtenidos del request (estado propuesto)
     */
    private Mono<SolicitudCambioServicioPartidaDTO>
            getOrdenProduccionPartida(GuardarSolicitudCambioRequestDTO request) {

        log.debug("🔄 Enriqueciendo request con datos reales de BD para partida: {}", request.getIdPartida());

        return comercialRepositoryAdapter.getInfoOrdenProduccionPartida(request.getIdPartida())
                .flatMap(datosReales -> {
                    log.debug("📊 Datos OLD obtenidos de BD:");
                    log.debug("   - id_ordenproduccion_old: {}", datosReales.getIdOrdenProduccion());
                    log.debug("   - id_orden_old: {}", datosReales.getIdOrden());
                    log.debug("   - id_det_os_old: {}", datosReales.getIdDetOs());
                    log.debug("   - precio_old: {}", datosReales.getPrecio());
                    log.debug("   - id_gama_old: {}", datosReales.getIdGama());
                    log.debug("   - id_ruta_old: {}", datosReales.getIdRuta());
                    log.debug("   - desc_articulo_old: {}", datosReales.getDescArticulo());

                    log.debug("📋 Datos NEW propuestos desde request:");
                    log.debug("   - id_ordenproduccion (NEW): {}", request.getSolicitudCambio().getIdOrdenproduccion());
                    log.debug("   - id_ruta (NEW): {}", request.getSolicitudCambio().getIdRuta());
                    log.debug("   - id_gama (NEW): {}", request.getSolicitudCambio().getIdGama());
                    log.debug("   - precio (NEW): {}", request.getSolicitudCambio().getPrecio());

                    // Construir el builder base con valores OLD y NEW
                    SolicitudCambioServicioPartidaDTO.SolicitudCambioServicioPartidaDTOBuilder dtoBuilder =
                        SolicitudCambioServicioPartidaDTO.builder()
                            // Datos básicos
                            .idPartida(request.getIdPartida())
                            // Valores OLD
                            .idOrdenproduccionOld(datosReales.getIdOrdenProduccion())
                            .idOrdenOld(datosReales.getIdOrden())
                            .idDetOsOld(datosReales.getIdDetOs())
                            .idPrecioOld(datosReales.getIdPrecio())
                            .precioOld(datosReales.getPrecio())
                            .idGamaOld(datosReales.getIdGama())
                            .idRutaOld(datosReales.getIdRuta())
                            .descArticuloOld(datosReales.getDescArticulo())
                            // Valores NEW
                            .idOrdenproduccion(request.getSolicitudCambio().getIdOrdenproduccion())
                            .idOrden(request.getSolicitudCambio().getIdOrden())
                            .idDetOs(request.getSolicitudCambio().getIdDetOs())
                            .idRuta(request.getSolicitudCambio().getIdRuta())
                            .idGama(request.getSolicitudCambio().getIdGama())
                            .idPrecio(request.getSolicitudCambio().getIdPrecio())
                            .precio(request.getSolicitudCambio().getPrecio())
                            .status(1)
                            .aprobado(0)
                            .porAprobar(1)
                            .partidasAdicionales((request.getAplicarOtrasPartidas())?1:0)
                            .idUsuario(1)
                            .fecRegistro(java.time.LocalDate.now());

                    // ===== VALIDACIÓN: Si aplicarOtrasPartidas es true, obtener partidas adicionales =====
                    if (request.getAplicarOtrasPartidas() != null && request.getAplicarOtrasPartidas()) {
                        log.debug("🔍 aplicarOtrasPartidas = true, obteniendo partidas adicionales de la orden: {}",
                                datosReales.getIdOrdenProduccion());

                        return comercialRepositoryAdapter.getStatusDespachoPartidas(datosReales.getIdOrdenProduccion())
                                .filter(partidaStatus -> {
                                    // Omitir la partida principal
                                    boolean esPartidaPrincipal = partidaStatus.getIdPartida().equals(request.getIdPartida());
                                    if (esPartidaPrincipal) {
                                        log.debug("⏭️ Omitiendo partida principal: {}", partidaStatus.getIdPartida());
                                    }
                                    return !esPartidaPrincipal;
                                })
                                .map(partidaStatus -> {
                                    // Mapear a SolicitudPartidaAdicionalDTO
                                    log.debug("📦 Mapeando partida adicional: idPartida={}, despachado={}",
                                            partidaStatus.getIdPartida(), partidaStatus.getCntDespachado());

                                    return com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.SolicitudPartidaAdicionalDTO.builder()
                                            .idSolicitud(null) // Se asignará después de guardar la solicitud principal
                                            .idPartida(partidaStatus.getIdPartida())
                                            .codPartida("PARTIDA-" + partidaStatus.getIdPartida()) // TODO: Obtener código real
                                            .status(1) // Activo
                                            .aprobado(0) // No aprobado inicialmente
                                            .createAt(java.time.LocalDateTime.now())
                                            .build();
                                })
                                .collectList()
                                .map(listaPartidasAdicionales -> {
                                    log.info("✅ {} partidas adicionales encontradas (omitiendo la principal)",
                                            listaPartidasAdicionales.size());

                                    // Asignar la lista al builder y construir
                                    return dtoBuilder
                                            .partidasAdicionalesDTO(listaPartidasAdicionales)
                                            .build();
                                });
                    } else {
                        log.debug("ℹ️ aplicarOtrasPartidas = false, no se cargan partidas adicionales");

                        // No hay partidas adicionales, construir directamente
                        return Mono.just(dtoBuilder
                                .partidasAdicionalesDTO(java.util.List.of())
                                .build());
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("❌ No se pudo enriquecer: No se encontraron datos para partida {}", request.getIdPartida());
                    return Mono.error(new IllegalArgumentException(
                            String.format("No se encontraron datos para la partida %d", request.getIdPartida())));
                }));
    }

    /**
     * Verifica que los datos del request coincidan con los datos actuales de la base de datos.
     * Esto ayuda a detectar cambios concurrentes realizados por otros usuarios.
     *
     * @param request Datos del request del frontend
     * @param datosReales Datos actuales de la base de datos
     * @return true si los datos coinciden, false si hay discrepancias
     */
    private boolean validarCoincidenciaDatos(GuardarSolicitudCambioRequestDTO request,
            OrdenProduccionPartidaDTO datosReales) {

        boolean coinciden = true;

        // Comparar orden de producción
        if (!datosReales.getIdOrdenProduccion().equals(request.getIdOrdenproduccion())) {
            log.debug("❌ idOrdenProduccion no coincide: BD={}, Request={}",
                    datosReales.getIdOrdenProduccion(), request.getIdOrdenproduccion());
            coinciden = false;
        }

        // Comparar código de orden de producción si está disponible
        if (datosReales.getCodOrdenProduccion() != null &&
            request.getSolicitudCambio().getCodOrdenproduccion() != null &&
            !datosReales.getCodOrdenProduccion().equals(request.getSolicitudCambio().getCodOrdenproduccion())) {
            log.debug("❌ codOrdenProduccion no coincide: BD={}, Request={}",
                    datosReales.getCodOrdenProduccion(), request.getSolicitudCambio().getCodOrdenproduccion());
            coinciden = false;
        }

        return coinciden;
    }
}
