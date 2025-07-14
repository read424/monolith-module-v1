package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence;

import java.util.List;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;

import com.walrex.avro.schemas.ItemGuiaRemisionRemitenteMessage;
import com.walrex.module_ecomprobantes.application.ports.output.DetalleComprobantePersistencePort;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper.DetalleComprobantePersistenceMapper;
import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.DetalleComprobanteEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador para persistir detalles de comprobantes usando R2DBC
 * 
 * CARACTERÍSTICAS:
 * - Implementa DetalleComprobantePersistencePort
 * - Usa MapStruct para mapeo automático
 * - Operaciones reactivas con Mono/Flux
 * - Logging estructurado para observabilidad
 * - Manejo de errores reactivo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DetalleComprobantePersistenceAdapter implements DetalleComprobantePersistencePort {

    private final R2dbcEntityTemplate r2dbcTemplate;
    private final DetalleComprobantePersistenceMapper detalleMapper;

    @Override
    public Mono<Void> crearDetallesComprobante(List<ItemGuiaRemisionRemitenteMessage> items, Long idComprobante) {
        if (items == null || items.isEmpty()) {
            log.info("📋 No hay items para crear detalles - Comprobante: {}", idComprobante);
            return Mono.empty();
        }

        log.info("💾 Creando {} detalles para comprobante: {}", items.size(), idComprobante);

        return Mono.fromCallable(() -> detalleMapper.toEntityList(items, idComprobante))
                .doOnNext(entities -> log.debug("📝 Entidades de detalle preparadas: {}", entities.size()))
                .flatMapMany(Flux::fromIterable)
                .flatMap(r2dbcTemplate::insert)
                .doOnNext(entity -> log.debug("✅ Detalle creado - ID: {}, Producto: {}, Comprobante: {}",
                        entity.getIdDetalleComprobante(), entity.getIdProducto(), entity.getIdComprobante()))
                .then()
                .doOnSuccess(
                        v -> log.info("✅ Todos los detalles creados exitosamente - Comprobante: {}", idComprobante))
                .doOnError(error -> log.error("❌ Error creando detalles - Comprobante: {}, Error: {}",
                        idComprobante, error.getMessage(), error));
    }

    @Override
    public Mono<Long> contarDetallesPorComprobante(Long idComprobante) {
        log.debug("🔍 Contando detalles para comprobante: {}", idComprobante);

        return r2dbcTemplate.count(
                org.springframework.data.relational.core.query.Query.query(
                        org.springframework.data.relational.core.query.Criteria.where("id_comprobante")
                                .is(idComprobante)),
                DetalleComprobanteEntity.class)
                .doOnNext(count -> log.debug("📊 Detalles encontrados para comprobante {}: {}", idComprobante, count))
                .doOnError(error -> log.error("❌ Error contando detalles - Comprobante: {}, Error: {}",
                        idComprobante, error.getMessage()));
    }

    @Override
    public Mono<Void> eliminarDetallesPorComprobante(Long idComprobante) {
        log.info("🗑️ Eliminando detalles para comprobante: {}", idComprobante);

        return r2dbcTemplate.delete(
                org.springframework.data.relational.core.query.Query.query(
                        org.springframework.data.relational.core.query.Criteria.where("id_comprobante")
                                .is(idComprobante)),
                DetalleComprobanteEntity.class)
                .then()
                .doOnSuccess(v -> log.info("✅ Detalles eliminados exitosamente - Comprobante: {}", idComprobante))
                .doOnError(error -> log.error("❌ Error eliminando detalles - Comprobante: {}, Error: {}",
                        idComprobante, error.getMessage()));
    }
}