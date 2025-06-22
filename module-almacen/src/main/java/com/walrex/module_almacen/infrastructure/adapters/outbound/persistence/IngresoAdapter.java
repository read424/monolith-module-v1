package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.RegistrarIngresoPort;
import com.walrex.module_almacen.domain.model.dto.DetalleIngresoDTO;
import com.walrex.module_almacen.domain.model.dto.ItemProductDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailsIngresoMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngresoAdapter implements RegistrarIngresoPort {
    private final OrdenIngresoRepository ordenIngresoRepository;
    private final DetailsIngresoRepository detailsIngresoRepository;
    private final DetailsIngresoMapper detailsIngresoMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Mono<OrdenIngresoDTO> registrarIngreso(
            Integer idMotivo, Integer idAlmacen,
            LocalDate fecha, String observacion,
            List<ItemProductDTO> items, String transactionId
    ) {
        MDC.put("correlationId", transactionId);
        log.info("Iniciando registro de ingreso [transactionId={}, almacén={}, motivo={}, items={}]",
                transactionId, idAlmacen, idMotivo, items != null ? items.size() : 0);

        if (items == null || items.isEmpty()) {
            log.warn("Lista de items vacía en registrarIngreso [transactionId={}]", transactionId);
        }

        return ordenIngresoRepository.agregarIngreso(idMotivo, observacion, fecha, idAlmacen)
                .doOnSubscribe(s -> log.debug("Creando orden de ingreso en base de datos [transactionId={}]", transactionId))
                .flatMap(result->{
                    Long idOrdenIngreso = result.getId();
                    log.info("Orden de ingreso creada con ID: {} [transactionId={}]",
                            idOrdenIngreso, transactionId);

                    // Asignamos el ID de la orden a cada item
                    items.forEach(item -> item.setId_orden(idOrdenIngreso));

                    log.debug("Mapeando {} items a entidades de detalles [transactionId={}]",
                            items.size(), transactionId);

                    // Convertimos los items a detalles de ingreso
                    List<DetailsIngresoEntity> detallesEntities = detailsIngresoMapper.toEntityList(items);

                    return Flux.fromIterable(detallesEntities)
                            .index()
                            //.flatMap(this::guardarDetalleIngreso)
                            .flatMap(tuple->{
                                long index = tuple.getT1();
                                DetailsIngresoEntity entity = tuple.getT2();
                                log.debug("Guardando detalle de ingreso #{} [artículo={}, unidad={}, cantidad={}] [transactionId={}]",
                                        index + 1, entity.getId_articulo(), entity.getId_unidad(),
                                        entity.getCantidad(), transactionId);

                                return guardarDetalleIngreso(entity)
                                        .doOnSuccess(saved -> log.debug("Detalle de ingreso #{} guardado correctamente [transactionId={}]",
                                                index + 1, transactionId))
                                        .doOnError(error -> log.error("Error al guardar detalle de ingreso #{} [transactionId={}]: {}",
                                                index + 1, transactionId, error.getMessage(), error));
                            })
                            .map(detailsIngresoMapper::toDto)
                            .collectList()
                            .map(detallesGuardados->{
                                log.info("Guardados {} detalles para orden de ingreso {}, transactionId: {}",
                                        detallesGuardados.size(), idOrdenIngreso, transactionId);
                                return OrdenIngresoDTO.builder()
                                        .id(idOrdenIngreso)
                                        .idMotivo(idMotivo)
                                        .idAlmacen(idAlmacen)
                                        .observacion(observacion)
                                        .fechaIngreso(fecha)
                                        .detalles(convertToDetalleIngresoList(detallesGuardados))
                                        .build();
                            });
                })
                .doOnSuccess(result -> log.info("Registro de ingreso completado exitosamente [orden={}, detalles={}] [transactionId={}]",
                    result.getId(), result.getDetalles().size(), transactionId)
                )
                .doOnError(error -> log.error("Error en registrarIngreso [transactionId={}]: {}",
                        transactionId, error.getMessage(), error)
                )
                .doFinally(signal -> MDC.remove("correlationId"));
    }

    private Mono<DetailsIngresoEntity> guardarDetalleIngreso(DetailsIngresoEntity entity) {
        return detailsIngresoRepository.addDetalleIngreso(
                entity.getId_ordeningreso().intValue(),
                entity.getId_articulo(),
                entity.getId_unidad(),
                entity.getCantidad(),
                entity.getCosto_compra());
    }

    private List<DetalleIngresoDTO> convertToDetalleIngresoList(List<ItemProductDTO> items) {
        return items.stream()
                .map(this::convertToDetalleIngreso)
                .collect(Collectors.toList());
    }


    private DetalleIngresoDTO convertToDetalleIngreso(ItemProductDTO item) {
        return DetalleIngresoDTO.builder()
                .idOrdenIngreso(item.getId_orden())
                .idArticulo(item.getId_articulo())
                .idUnidad(item.getId_unidad())
                .cantidad(item.getCantidad())
                .costoCompra(item.getPrecio())
                .observacion(item.getObservacion())
                .build();
    }
}
