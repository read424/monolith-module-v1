package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_almacen.application.ports.output.GuiaRemisionPersistencePort;
import com.walrex.module_almacen.domain.model.dto.DetailItemGuiaRemisionDTO;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionServiciosEntity;
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
                                .then(obtenerDatosGuiaGenerada(request.getIdOrdenSalida()))
                                .doOnNext(guia -> log.info(
                                                "✅ Guía de remisión generada exitosamente - Orden: {}, Fecha: {}",
                                                guia.getIdOrdenSalida(), guia.getFechaEntrega()))
                                .doOnError(error -> log.error("❌ Error al generar guía de remisión en BD: {}",
                                                error.getMessage()));
        }

        @Override
        public Mono<Boolean> validarOrdenSalidaParaGuia(Long idOrdenSalida) {
                return devolucionServiciosRepository.findByIdOrdenSalida(idOrdenSalida.intValue())
                                .switchIfEmpty(
                                                Mono.error(new IllegalArgumentException(
                                                                "No se encontro orden de salida para esta devolucion")))
                                .doOnNext(devolucionServicio -> {
                                        log.debug("🔍 Validando condiciones para orden: {}", devolucionServicio);
                                        // ✅ Validaciones en doOnNext para mejor legibilidad
                                        validarGuiaNoAsignada(devolucionServicio);
                                        validarOrdenNoAnulada(devolucionServicio);
                                        validarOrdenNoDespachada(devolucionServicio);
                                })
                                .map(devolucionServicio -> {
                                        log.debug("✅ Orden válida para generar guía: {}", idOrdenSalida);
                                        return true;
                                })
                                .doOnError(error -> log.warn("❌ Validación fallida para orden {}: {}",
                                                idOrdenSalida, error.getMessage()));
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
                DevolucionServiciosEntity devolucionServiciosEntity = guiaRemisionEntityMapper.DTOtoEntity(request);

                return devolucionServiciosRepository.save(devolucionServiciosEntity)
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
        private Mono<Void> actualizarFechaEntregaOrdenSalida(GuiaRemisionGeneradaDTO request) {
                log.info("📅 Actualizando fecha de entrega en ordensalida con Id: {}", request.getIdOrdenSalida());

                return ordenSalidaRepository
                                .assignedEntregadoDevolucion(request.getFechaEntrega(),
                                                request.getIdOrdenSalida().intValue())
                                .doOnSuccess(
                                                result -> log.info(
                                                                "✅ Fecha de entrega actualizada exitosamente para orden: {}",
                                                                result))
                                .doOnError(error -> log.error(
                                                "❌ Error al actualizar fecha de entrega para orden {}: {}",
                                                request.getIdOrdenSalida(), error.getMessage()))
                                .onErrorMap(DataAccessException.class,
                                                ex -> new RuntimeException(
                                                                "Error al actualizar fecha de entrega en ordensalida",
                                                                ex))
                                .then(); // Convertir cualquier resultado a Mono<Void>
        }

        /**
         * Obtiene los datos de la guía de remisión generada después de las
         * actualizaciones
         *
         * @param idOrdenSalida ID de la orden de salida
         * @return Mono<GuiaRemisionGeneradaDTO> con los datos actualizados
         */
        private Mono<GuiaRemisionGeneradaDTO> obtenerDatosGuiaGenerada(Long idOrdenSalida) {
                log.info("📋 Obteniendo datos de guía generada para orden: {}", idOrdenSalida);

                return devolucionServiciosRepository.findByIdOrdenSalidaEnabled(idOrdenSalida.intValue())
                                .switchIfEmpty(Mono.error(new IllegalStateException(
                                                "No se encontraron datos de devolución para la orden: "
                                                                + idOrdenSalida)))
                                .flatMap(devolucionEntity -> {
                                        // ✅ Mapear datos básicos de devolución
                                        GuiaRemisionGeneradaDTO guiaRemisionGeneradaDTO = devolucionServicioEntityMapper
                                                        .toDto(devolucionEntity);

                                        // ✅ Obtener datos de orden de salida para completar idCliente y codigoSalida
                                        return ordenSalidaRepository
                                                        .findByIdOrdenSalidaEnabled(idOrdenSalida.intValue())
                                                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                        "No se encontró la orden de salida: "
                                                                                        + idOrdenSalida)))
                                                        .flatMap(ordenEntity -> {
                                                                // ✅ Completar datos de la orden de salida
                                                                guiaRemisionGeneradaDTO.setIdCliente(
                                                                                ordenEntity.getId_cliente());
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

                                                                                        // ✅ Setear la lista de items en
                                                                                        // el DTO
                                                                                        guiaRemisionGeneradaDTO
                                                                                                        .setDetailItems(detailItems);

                                                                                        log.debug("✅ Items mapeados exitosamente: {} items para orden: {}",
                                                                                                        detailItems.size(),
                                                                                                        idOrdenSalida);

                                                                                        return guiaRemisionGeneradaDTO;
                                                                                });
                                                        });
                                })
                                .doOnNext(guiaGenerada -> log.info("✅ Datos de guía obtenidos exitosamente: {}",
                                                guiaGenerada))
                                .doOnError(error -> log.error("❌ Error al obtener datos de guía generada: {}",
                                                error.getMessage()));
        }
}
