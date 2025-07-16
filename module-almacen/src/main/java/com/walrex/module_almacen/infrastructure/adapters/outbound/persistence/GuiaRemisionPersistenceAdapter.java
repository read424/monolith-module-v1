package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_almacen.application.ports.output.GuiaRemisionPersistencePort;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionServiciosEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DevolucionServicioEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.GuiaRemisionEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuiaRemisionPersistenceAdapter implements GuiaRemisionPersistencePort {
        private final DevolucionServiciosRepository devolucionServiciosRepository;
        private final OrdenSalidaRepository ordenSalidaRepository;
        private final DetailSalidaRepository detailSalidaRepository;
        private final GuiaRemisionEntityMapper guiaRemisionEntityMapper;
        private final DevolucionServicioEntityMapper devolucionServicioEntityMapper;

        @Override
        @Transactional
        public Mono<GuiaRemisionGeneradaDTO> generarGuiaRemision(GuiaRemisionGeneradaDTO request) {
                log.info("🚚 Generando guía de remisión en BD para orden: {}", request.getIdOrdenSalida());

                return actualizarDevolucionServicios(request)
                                .then(actualizarFechaEntregaOrdenSalida(request))
                                .then(Mono.just(request))
                                .doOnNext(guia -> log.info(
                                                "✅ Guía de remisión persistida exitosamente - Orden: {}, Fecha: {}, Cliente: {}",
                                                guia.getIdOrdenSalida(), guia.getFechaEntrega(), guia.getIdCliente()))
                                .doOnError(error -> log.error("❌ Error al persistir guía de remisión en BD: {}",
                                                error.getMessage()));
        }

        @Override
        public Mono<Boolean> validarOrdenSalidaParaGuia(GuiaRemisionGeneradaDTO ordenSalida) {
                return devolucionServiciosRepository.findByIdOrdenSalida(ordenSalida.getIdOrdenSalida().intValue())
                                .switchIfEmpty(
                                                Mono.error(new IllegalArgumentException(
                                                                "No se encontro orden de salida para esta devolucion")))
                                .doOnNext(devolucionServicio -> {
                                        log.debug("🔍 Validando condiciones para orden: {}", devolucionServicio);
                                        // ✅ Validaciones en doOnNext para mejor legibilidad
                                        validarGuiaNoAsignada(devolucionServicio);
                                        validarOrdenNoAnulada(devolucionServicio);
                                        validarOrdenNoDespachada(devolucionServicio);
                                        ordenSalida.setIdDevolucion(devolucionServicio.getId());
                                        ordenSalida.setStatus(devolucionServicio.getStatus());
                                        ordenSalida.setIdComprobante(devolucionServicio.getIdComprobante());
                                })
                                .map(devolucionServicio -> {
                                        log.debug("✅ Orden válida para generar guía: {}",
                                                        ordenSalida.getIdOrdenSalida());
                                        return true;
                                })
                                .doOnError(error -> log.warn("❌ Validación fallida para orden {}: {}",
                                                ordenSalida.getIdOrdenSalida(), error.getMessage()));
        }

        // ✅ Métodos helper para validaciones específicas
        private void validarGuiaNoAsignada(DevolucionServiciosEntity devolucionServicio) {
                if (devolucionServicio.getIdComprobante() != null) {
                        throw new IllegalStateException(
                                        "La Orden de Salida para Devolucion ya tiene asignado una guia");
                }
        }

        private void validarOrdenNoAnulada(DevolucionServiciosEntity devolucionServicio) {
                if (devolucionServicio.getStatus() != null && devolucionServicio.getStatus() == 0) {
                        throw new IllegalStateException("La Orden de Salida para Devolucion fue anulada");
                }
        }

        private void validarOrdenNoDespachada(DevolucionServiciosEntity devolucionServicio) {
                if (devolucionServicio.getEntregado() != null && devolucionServicio.getEntregado() == 1) {
                        throw new IllegalStateException("La orden de Salida para Devolucion ya fue despachada");
                }
        }

        private Mono<DevolucionServiciosEntity> actualizarDevolucionServicios(GuiaRemisionGeneradaDTO request) {
                log.info("🔄 Actualizando devolucion_servicios para orden: {}", request);

                return devolucionServiciosRepository.updateDevolucionServicios(
                                request.getIdDevolucion(),
                                request.getIdMotivoComprobante(),
                                request.getIdComprobante(),
                                request.getIdEmpresaTransp(),
                                request.getIdModalidad(),
                                request.getIdTipDocChofer(),
                                request.getNumDocChofer(),
                                request.getNumPlaca(),
                                request.getIdLlegada(),
                                request.getFechaEntrega(),
                                request.getIdUsuario())
                                .doOnNext(
                                                savedEntity -> log.info(
                                                                "✅ DevolucionServicios actualizado exitosamente - ID: {}, Orden: {}",
                                                                savedEntity.getId(), savedEntity
                                                                                .getIdOrdenSalida()))
                                .doOnError(error -> log.error(
                                                "❌ Error final al actualizar devolucion_servicios para orden {}: {}",
                                                request.getIdOrdenSalida(), error.getMessage()));
        }

        /**
         * Actualiza la fecha de entrega en la tabla ordensalida
         *
         * @param idOrdenSalida ID de la orden de salida
         * @return Mono<Void> confirmación de la actualización
         */
        private Mono<OrdenSalidaEntity> actualizarFechaEntregaOrdenSalida(GuiaRemisionGeneradaDTO request) {
                log.info("📅 Actualizando fecha de entrega en ordensalida con Id: {}", request.getIdOrdenSalida());

                return ordenSalidaRepository
                                .assignedEntregadoDevolucion(request.getFechaEntrega(),
                                                request.getIdOrdenSalida().intValue())
                                .doOnNext(ordenSalida -> {
                                        log.info("✅ Fecha de entrega actualizada exitosamente para orden: {}",
                                                        ordenSalida.getId());
                                        // ✅ Setear el idCliente en el DTO desde la entidad actualizada
                                        request.setIdCliente(ordenSalida.getId_cliente());
                                        log.debug("👤 ID Cliente seteado en DTO: {} para orden: {}",
                                                        ordenSalida.getId_cliente(), ordenSalida.getId());
                                })
                                .doOnError(error -> log.error(
                                                "❌ Error al actualizar fecha de entrega para orden {}: {}",
                                                request.getIdOrdenSalida(), error.getMessage()))
                                .onErrorMap(DataAccessException.class,
                                                ex -> new RuntimeException(
                                                                "Error al actualizar fecha de entrega en ordensalida",
                                                                ex));
        }

        /**
         * Obtiene los datos de la guía de remisión generada después de las
         * actualizaciones
         *
         * @param idOrdenSalida ID de la orden de salida
         * @return Mono<GuiaRemisionGeneradaDataDTO> con los datos actualizados
         */
        @Override
        public Mono<GuiaRemisionGeneradaDataDTO> obtenerDatosGuiaGenerada(Long idOrdenSalida) {
                log.info("📋 Obteniendo datos de guía generada para orden: {}", idOrdenSalida);

                return devolucionServiciosRepository.findByIdOrdenSalidaEnabled(idOrdenSalida.intValue())
                                .switchIfEmpty(Mono.error(new IllegalStateException(
                                                "No se encontraron datos de devolución para la orden: "
                                                                + idOrdenSalida)))
                                .flatMap(devolucionEntity -> {
                                        log.info("🔍 Datos de devolución encontrados: {}", devolucionEntity);
                                        if (devolucionEntity.getIdComprobante() != null) {
                                                throw new IllegalStateException(
                                                                "El idComprobante ya fue asignado a la orden de salida");
                                        }
                                        // ✅ Obtener datos de la orden de salida para el idCliente
                                        return ordenSalidaRepository
                                                        .findByIdOrdenSalidaEnabled(idOrdenSalida.intValue())
                                                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                        "No se encontró la orden de salida: "
                                                                                        + idOrdenSalida)))
                                                        .flatMap(ordenEntity -> {
                                                                // ✅ Obtener artículos devueltos desde
                                                                // DetailSalidaRepository
                                                                return detailSalidaRepository
                                                                                .findByIdOrderSalida(idOrdenSalida)
                                                                                .collectList()
                                                                                .map(articulosDevueltos -> {
                                                                                        log.debug("📦 Artículos devueltos encontrados: {} para orden: {}",
                                                                                                        articulosDevueltos
                                                                                                                        .size(),
                                                                                                        idOrdenSalida);

                                                                                        // ✅ Mapear entidades a DTOs
                                                                                        // usando el mapper
                                                                                        List<DetailItemGuiaRemisionDTO> detailItems = articulosDevueltos
                                                                                                        .stream()
                                                                                                        .map(devolucionServicioEntityMapper::toDetailItemGuiaRemisionDTO)
                                                                                                        .toList();

                                                                                        // ✅ Crear el DTO específico
                                                                                        // para datos de guía generada
                                                                                        GuiaRemisionGeneradaDataDTO guiaDataDTO = GuiaRemisionGeneradaDataDTO
                                                                                                        .builder()
                                                                                                        .idCliente(ordenEntity
                                                                                                                        .getId_cliente())
                                                                                                        .idOrdenSalida(ordenEntity
                                                                                                                        .getId()
                                                                                                                        .intValue())
                                                                                                        .idMotivo(devolucionEntity
                                                                                                                        .getIdMotivoComprobante())
                                                                                                        .detailItems(detailItems)
                                                                                                        .build();

                                                                                        log.debug("✅ Datos de guía mapeados exitosamente: {} items para orden: {}",
                                                                                                        detailItems.size(),
                                                                                                        idOrdenSalida);

                                                                                        return guiaDataDTO;
                                                                                });
                                                        });
                                })
                                .doOnNext(guiaData -> log.info("✅ Datos de guía obtenidos exitosamente: {}",
                                                guiaData))
                                .doOnError(error -> log.error("❌ Error al obtener datos de guía generada: {}",
                                                error.getMessage()));
        }
}
