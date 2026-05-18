package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.common.Exception.OrdenIngresoException;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.ArticuloInventory;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import io.r2dbc.spi.R2dbcBadGrammarException;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import io.r2dbc.spi.R2dbcException;
import io.r2dbc.spi.R2dbcTransientResourceException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
@RequiredArgsConstructor
@Slf4j
public abstract class BaseOrdenIngresoPersistenceAdapter implements OrdenIngresoLogisticaPort {
    protected final OrdenIngresoRepository ordenIngresoRepository;
    protected final ArticuloAlmacenRepository articuloRepository;
    protected final DetailsIngresoRepository detalleRepository;
    protected final OrdenIngresoEntityMapper mapper;
    protected final ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper;

    @Override
    @Transactional
    public Mono<OrdenIngreso> guardarOrdenIngresoLogistica(OrdenIngreso ordenIngreso) {
        log.info("Guardando orden de ingreso en la base de datos");
        // Validar que existan detalles
        if (ordenIngreso.getDetalles() == null || ordenIngreso.getDetalles().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La orden de ingreso debe tener al menos un detalle"));
        }
        // Convertir modelo de dominio a entidad usando el mapper
        OrdenIngresoEntity entity = mapper.toEntity(ordenIngreso);
        return ordenIngresoRepository.save(entity)
                .doOnSuccess(savedEntity ->
                    log.info("✅ Información de orden guardada: {}", savedEntity)
                )
                // ✅ AGREGAR: Buscar la entidad completa después del save
                .flatMap(savedEntity ->
                        ordenIngresoRepository.findById(savedEntity.getId())
                                .doOnSuccess(refreshedEntity ->
                                        log.info("🔄 Información de orden refrescada: {}", refreshedEntity)
                                )
                )
                .onErrorResume(R2dbcException.class, ex -> {
                    String prefix;
                    if (ex instanceof R2dbcDataIntegrityViolationException) {
                        prefix = "Error de integridad de datos";
                    } else if (ex instanceof R2dbcBadGrammarException) {
                        prefix = "Error de sintaxis SQL";
                    } else if (ex instanceof R2dbcTransientResourceException) {
                        prefix = "Error transitorio de recursos";
                    } else {
                        prefix = "Error de base de datos";
                    }
                    String errorMsg = prefix + " al guardar la orden: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new OrdenIngresoException(errorMsg, ex));
                })
                .onErrorResume(OrdenIngresoException.class, Mono::error)
                .onErrorResume(Exception.class, ex->{
                    String errorMsg = "Error no esperado al guardar la orden: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new OrdenIngresoException(errorMsg, ex));
                })
                .flatMap(savedEntity -> procesarDetalles(ordenIngreso, savedEntity));
    }

    // Método para procesar detalles, que ahora delega en el método específico
    private Mono<OrdenIngreso> procesarDetalles(OrdenIngreso ordenIngreso, OrdenIngresoEntity savedEntity) {
        // Actualizar IDs en la orden
        ordenIngreso.setId(savedEntity.getId().intValue());
        ordenIngreso.setCod_ingreso(savedEntity.getCod_ingreso());

        // Procesar cada detalle
        List<Mono<DetalleOrdenIngreso>> detallesMonos = ordenIngreso.getDetalles().stream()
                .map(detalle -> procesarDetalle(detalle, ordenIngreso))
                .collect(Collectors.toList());

        // Guardar todos los detalles y retornar la orden completa
        return Flux.merge(detallesMonos)
                .collectList()
                .map(detallesGuardados -> {
                    OrdenIngreso ordenCompleta = mapper.toDomain(savedEntity);
                    ordenCompleta.setDetalles(detallesGuardados);
                    return ordenCompleta;
                });
    }

    // Método para procesar un detalle individual
    protected Mono<DetalleOrdenIngreso> procesarDetalle(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
        if (detalle.getArticulo().getStock() == null) {
            return buscarInfoConversion(detalle, ordenIngreso)
                    .flatMap(infoConversion -> aplicarConversion(detalle, infoConversion))
                    .flatMap(detalleConvertido -> guardarDetalleOrdenIngreso(detalleConvertido, ordenIngreso));
        } else {
            return guardarDetalleOrdenIngreso(detalle, ordenIngreso);
        }
    }

    // Método para buscar información de conversión
    protected Mono<ArticuloInventory> buscarInfoConversion(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
        return articuloRepository.getInfoConversionArticulo(
                    ordenIngreso.getAlmacen().getIdAlmacen(),
                    detalle.getArticulo().getId()
                )
                .doOnNext(info -> log.info("✅ Información de conversión encontrada: {}", info))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se encontró información de conversión para el artículo: " +
                                detalle.getArticulo().getId()
                    ))
                );
    }

    // Método para aplicar conversión
    protected Mono<DetalleOrdenIngreso> aplicarConversion(DetalleOrdenIngreso detalle, ArticuloInventory infoConversion) {
        if (!detalle.getIdUnidad().equals(infoConversion.getIdUnidadConsumo())) {
            detalle.setIdUnidadSalida(infoConversion.getIdUnidadConsumo());
            detalle.getArticulo().setIs_multiplo(infoConversion.getIsMultiplo());
            detalle.getArticulo().setValor_conv(infoConversion.getValorConv());
            detalle.getArticulo().setStock(infoConversion.getStock());
        } else {
            detalle.setIdUnidadSalida(detalle.getIdUnidad());
        }
        return Mono.just(detalle);
    }

    // Método auxiliar para encapsular la lógica de guardar un detalle
    protected Mono<DetalleOrdenIngreso> guardarDetalleOrdenIngreso(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
        DetailsIngresoEntity detalleEntity = articuloIngresoLogisticaMapper.toEntity(detalle);
        detalleEntity.setId_ordeningreso(ordenIngreso.getId().longValue());

        return detalleRepository.save(detalleEntity)
                .doOnSuccess(info ->
                        log.debug("✅ Información de detalle articulo guardado: {}", info)
                )
                .onErrorResume(ex -> manejarErroresGuardadoDetalle(ex, detalle))
                .flatMap(savedDetalleEntity ->
                        // Este método será implementado por las subclases
                        procesarDetalleGuardado(detalle, savedDetalleEntity, ordenIngreso)
                );
    }

    // Método abstracto que implementarán las subclases
    protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
            DetalleOrdenIngreso detalle,
            DetailsIngresoEntity savedDetalleEntity,
            OrdenIngreso ordenIngreso) {
        return null;
    }

    // Método base que será usado por las implementaciones
    protected Mono<DetalleOrdenIngreso> actualizarIdDetalle(DetalleOrdenIngreso detalle, DetailsIngresoEntity savedDetalleEntity) {
        detalle.setId(savedDetalleEntity.getId().intValue());
        return Mono.just(detalle);
    }

    private Mono<DetailsIngresoEntity> manejarErroresGuardadoDetalle(Throwable ex, DetalleOrdenIngreso detalle) {
        log.error("Error al guardar detalle para artículo {}: Tipo: {}, Mensaje: {}",
                detalle.getArticulo().getId(),
                ex.getClass().getName(),
                ex.getMessage(),
                ex
        );
        return Mono.error(new OrdenIngresoException("Error al registrar detalle de ingreso", ex));
    }
}
