package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import com.walrex.module_ecomprobantes.application.ports.output.ComprobantePersistencePort;
import com.walrex.module_ecomprobantes.domain.model.dto.ComprobanteDTO;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.ComprobantePersistenceMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.DetalleComprobanteDTOMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.ComprobanteEntity;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.DetalleComprobanteEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComprobantePersistenceAdapter implements ComprobantePersistencePort {

        private final R2dbcEntityTemplate r2dbcTemplate;
        private final ComprobantePersistenceMapper comprobantePersistenceMapper;
        private final DetalleComprobanteDTOMapper detalleMapper;

        @Override
        public Mono<ComprobanteDTO> crearComprobante(ComprobanteDTO comprobante) {
                log.info("💾 Creando comprobante para cliente: {} - Tipo: {}",
                                comprobante.getIdCliente(), comprobante.getIdTipoComprobante());

                return Mono.fromCallable(() -> comprobantePersistenceMapper.toEntity(comprobante))
                                .flatMap(r2dbcTemplate::insert)
                                .map(comprobantePersistenceMapper::toDTO)
                                .doOnNext(resultado -> log.info("✅ Comprobante creado con ID: {} - Cliente: {}",
                                                resultado.getIdComprobante(), resultado.getIdCliente()))
                                .doOnError(error -> log.error("❌ Error creando comprobante: {}", error.getMessage()));
        }

        @Override
        public Mono<ComprobanteDTO> buscarComprobantePorId(Long idComprobante) {
                log.debug("🔍 Buscando comprobante por ID: {}", idComprobante);

                return r2dbcTemplate.selectOne(
                                org.springframework.data.relational.core.query.Query.query(
                                                org.springframework.data.relational.core.query.Criteria
                                                                .where("id_comprobante")
                                                                .is(idComprobante)),
                                ComprobanteEntity.class)
                                .map(comprobantePersistenceMapper::toDTO)
                                .doOnNext(comprobante -> log.debug("✅ Comprobante encontrado: {}",
                                                comprobante.getIdComprobante()))
                                .doOnError(
                                                error -> log.error("❌ Error buscando comprobante {}: {}", idComprobante,
                                                                error.getMessage()));
        }

        @Override
        public Mono<ComprobanteDTO> buscarComprobanteConDetallesPorId(Long idComprobante) {
                log.debug("🔍 Buscando comprobante con detalles por ID: {}", idComprobante);

                return buscarComprobantePorId(idComprobante)
                                .flatMap(comprobante -> cargarDetallesComprobante(comprobante))
                                .doOnNext(comprobante -> log.debug("✅ Comprobante con {} detalles encontrado: {}",
                                                comprobante.getDetalles().size(), comprobante.getIdComprobante()));
        }

        @Override
        public Mono<Void> actualizarEstadoComprobante(Long idComprobante, Integer nuevoEstado) {
                log.info("🔄 Actualizando estado comprobante {} a estado: {}", idComprobante, nuevoEstado);

                return r2dbcTemplate.update(
                                org.springframework.data.relational.core.query.Query.query(
                                                org.springframework.data.relational.core.query.Criteria
                                                                .where("id_comprobante")
                                                                .is(idComprobante)),
                                org.springframework.data.relational.core.query.Update.update("status", nuevoEstado),
                                ComprobanteEntity.class)
                                .then()
                                .doOnSuccess(v -> log.info("✅ Estado actualizado para comprobante: {}", idComprobante))
                                .doOnError(error -> log.error("❌ Error actualizando estado comprobante {}: {}",
                                                idComprobante,
                                                error.getMessage()));
        }

        @Override
        public Mono<ComprobanteDTO> actualizarComprobanteCompleto(ComprobanteDTO comprobante) {
                log.info("🔄 Actualizando comprobante completo: {} - Detalles: {}",
                                comprobante.getIdComprobante(), comprobante.getDetalles());

                return Mono.fromCallable(() -> comprobantePersistenceMapper.toEntity(comprobante))
                                .flatMap(entity -> r2dbcTemplate.update(entity))
                                .map(comprobantePersistenceMapper::toDTO)
                                .flatMap(comprobanteActualizado -> actualizarDetallesComprobante(comprobanteActualizado,
                                                comprobante.getDetalles()))
                                .doOnNext(resultado -> log.info("✅ Comprobante completo actualizado: {} - Detalles: {}",
                                                resultado.getIdComprobante(), resultado.getDetalles()))
                                .doOnError(error -> log.error("❌ Error actualizando comprobante completo {}: {}",
                                                comprobante.getIdComprobante(), error.getMessage()));
        }

        /**
         * Carga los detalles de un comprobante y los agrega al DTO
         * 
         * @param comprobante ComprobanteDTO al que agregar detalles
         * @return Mono<ComprobanteDTO> con detalles cargados
         */
        private Mono<ComprobanteDTO> cargarDetallesComprobante(ComprobanteDTO comprobante) {
                return r2dbcTemplate.select(
                                org.springframework.data.relational.core.query.Query.query(
                                                org.springframework.data.relational.core.query.Criteria
                                                                .where("id_comprobante")
                                                                .is(comprobante.getIdComprobante())),
                                DetalleComprobanteEntity.class)
                                .map(detalleMapper::toDTO)
                                .collectList()
                                .map(detalles -> {
                                        comprobante.getDetalles().clear();
                                        return comprobante;
                                })
                                .doOnNext(resultado -> log.debug("📋 Cargados {} detalles para comprobante: {}",
                                                resultado.getDetalles(), resultado.getIdComprobante()));
        }

        /**
         * Actualiza los detalles de un comprobante
         * 
         * @param comprobante    ComprobanteDTO base
         * @param nuevosDetalles Lista de nuevos detalles
         * @return Mono<ComprobanteDTO> con detalles actualizados
         */
        private Mono<ComprobanteDTO> actualizarDetallesComprobante(ComprobanteDTO comprobante,
                        java.util.List<com.walrex.module_ecomprobantes.domain.model.dto.DetalleComprobanteDTO> nuevosDetalles) {
                if (nuevosDetalles == null || nuevosDetalles.isEmpty()) {
                        return Mono.just(comprobante);
                }

                // Primero eliminar detalles existentes
                return r2dbcTemplate.delete(
                                org.springframework.data.relational.core.query.Query.query(
                                                org.springframework.data.relational.core.query.Criteria
                                                                .where("id_comprobante")
                                                                .is(comprobante.getIdComprobante())),
                                DetalleComprobanteEntity.class)
                                .then(
                                                // Luego insertar nuevos detalles
                                                Mono.fromCallable(() -> {
                                                        return nuevosDetalles.stream()
                                                                        .peek(detalle -> detalle.setIdComprobante(
                                                                                        comprobante.getIdComprobante()))
                                                                        .map(detalleMapper::toEntity)
                                                                        .toList();
                                                })
                                                                .flatMapMany(entidades -> reactor.core.publisher.Flux
                                                                                .fromIterable(entidades)
                                                                                .flatMap(r2dbcTemplate::insert))
                                                                .collectList())
                                .then(cargarDetallesComprobante(comprobante))
                                .doOnNext(resultado -> log.debug(
                                                "🔄 Detalles actualizados para comprobante: {} - Total: {}",
                                                resultado.getIdComprobante(), resultado.getDetalles()));
        }
}