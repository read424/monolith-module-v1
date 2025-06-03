package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.RegistrarEgresoPort;
import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Motivo;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaLoteEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailSalidaLoteRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailSalidaRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class EgresoAdapter implements RegistrarEgresoPort {
    private final OrdenSalidaRepository ordenEgresoRepository;
    private final DetailSalidaRepository detalleEgresoRepository;
    private final DetailSalidaLoteRepository salidaLoteRepository;
    private final DetailSalidaMapper detailSalidaMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Mono<OrdenEgresoDTO> registrarEgreso(
            Integer idMotivo,
            Integer idAlmacen,
            LocalDate fecha,
            String observacion,
            List<DetalleEgresoDTO> items,
            String transactionId
    ) {
        MDC.put("correlationId", transactionId);
        Integer idUsuario=26;
        Integer entregado=1;

        log.info("Iniciando registro de egreso. AlmacenId: {}, MotivoId: {}, Items: {}, TransactionId: {}",
                idAlmacen, idMotivo, items.size(), transactionId);

        if (items.isEmpty()) {
            log.warn("Intento de registrar egreso sin ítems. TransactionId: {}", transactionId);
        }
        return ordenEgresoRepository.agregarOrdenSalida(idMotivo, idAlmacen, idUsuario, fecha, entregado)
            .doOnSubscribe(s -> log.debug("Creando orden de salida en base de datos. TransactionId: {}", transactionId))
            .flatMap(result -> {
                Integer idOrdenSalida = result.getId_ordensalida().intValue();
                log.info("Orden de egreso creada con ID: {}, transactionId: {}", idOrdenSalida, transactionId);
                // Asignamos el ID de la orden a cada item
                items.forEach(item -> item.setIdOrdenEgreso(idOrdenSalida.longValue()));
                log.debug("Procesando {} ítems para la orden de egreso {}. TransactionId: {}",
                        items.size(), idOrdenSalida, transactionId);

                return Flux.fromIterable(items)
                    .doOnNext(item -> log.debug("Procesando ítem: ArticuloId: {}, Cantidad: {}, UnidadId: {}. TransactionId: {}",
                            item.getArticulo().getId(), item.getCantidad(), item.getIdUnidad(), transactionId)
                    )
                    .flatMap(item -> {
                        //Convertimos el DTO a entidad
                        DetailSalidaEntity detalleEntity = detailSalidaMapper.toEntity(item);

                        detalleEntity.setId_ordensalida(result.getId_ordensalida());
                        detalleEntity.setEntregado(entregado);
                        detalleEntity.setStatus(1);
                        return registrarDetalleSalida(detalleEntity)
                                .flatMap(detalleSalidaGuardado->{
                                   if(item.getA_lotes()!=null && !item.getA_lotes().isEmpty()){
                                       log.debug("El ítem con artículo {} tiene {} lotes para procesar. TransactionId: {}",
                                               item.getArticulo().getId(), item.getA_lotes().size(), transactionId);

                                       return procesarLotes(detalleSalidaGuardado, item.getA_lotes())
                                           .collectList()
                                           .doOnSuccess(lotes -> {
                                               log.debug("Procesados {} lotes para el artículo {} en detalle {}. TransactionId: {}",
                                                       lotes.size(), item.getArticulo().getId(), detalleSalidaGuardado.getId_detalle_orden(), transactionId);
                                           })
                                           .map(lotesSalida->{
                                               DetalleEgresoDTO itemActualizado = detailSalidaMapper.toDto(detalleSalidaGuardado);
                                               itemActualizado.setA_lotes(item.getA_lotes()); // Preservamos la info de lotes
                                              return itemActualizado;
                                           });
                                   }else{
                                       log.debug("El ítem con artículo {} no tiene lotes asociados. TransactionId: {}",
                                               item.getArticulo().getId(), transactionId);
                                       return Mono.just(detailSalidaMapper.toDto(detalleSalidaGuardado));
                                   }
                                });
                    })
                    .collectList()
                    .map(itemsActualizados->{
                        log.info("Guardados {} detalles para orden de egreso {}, transactionId: {}",
                                itemsActualizados.size(), idOrdenSalida, transactionId);
                        return OrdenEgresoDTO.builder()
                                .id(idOrdenSalida.longValue())
                                .motivo(Motivo.builder()
                                        .idMotivo(idMotivo)
                                        .build()
                                )
                                .almacenOrigen(Almacen.builder()
                                        .idAlmacen(idAlmacen)
                                        .build()
                                )
                                .observacion(observacion)
                                .fecRegistro(fecha)
                                .detalles(itemsActualizados)
                                .build();
                    }).doOnError(error -> {
                        log.error("A Error al registrar egreso: {} - transactionId: {}", error.getMessage(), transactionId);
                    });
            })
            .doOnSuccess(result -> {
                log.info("Registro de egreso completado exitosamente. OrdenId: {}, DetallesCount: {}, TransactionId: {}",
                        result.getId(), result.getDetalles().size(), transactionId);
            })
            .doOnError(error->{
                log.error("Error al registrar egreso: {} - transactionId: {}", error.getMessage(), transactionId);
            })
            .doFinally(signalType -> {
                log.debug("Finalizando operación de registro de egreso con señal: {}. TransactionId: {}",
                        signalType, transactionId);
                MDC.remove("correlationId");
            });
    }

    private Mono<DetailSalidaEntity> registrarDetalleSalida(DetailSalidaEntity entity) {
        // Asegurar que tenemos un total calculado
        if (entity.getTot_monto() == null && entity.getPrecio() != null && entity.getCantidad() != null) {
            entity.setTot_monto(entity.getPrecio() * entity.getCantidad());
            log.debug("Calculando monto total para artículo {}: {} x {} = {}",
                    entity.getId_articulo(), entity.getPrecio(), entity.getCantidad(), entity.getTot_monto());
        }
        log.debug("Registrando detalle de salida para artículo {}, cantidad {}, unidad {}",
                entity.getId_articulo(), entity.getCantidad(), entity.getId_unidad());
        return detalleEgresoRepository.agregarDetalleSalida(
                entity.getId_ordensalida().intValue(),
                entity.getId_articulo(),
                entity.getId_unidad(),
                entity.getCantidad(),
                entity.getEntregado(),
                entity.getPrecio(),
                entity.getTot_monto(),
                entity.getStatus()
            )
            .switchIfEmpty(Mono.error(new RuntimeException("No se pudo guardar la salida del articulo con id: "+entity.getId_articulo())))
            .doOnSuccess(result -> {
                log.debug("Detalle de salida registrado exitosamente. DetalleId: {}, ArticuloId: {}",
                        result.getId_detalle_orden(), entity.getId_articulo());
            })
            .onErrorResume(error -> {
                log.error("Error al guardar detalle de salida para artículo {}: {}",
                        entity.getId_articulo(), error.getMessage());
                return Mono.error(new RuntimeException("Error al guardar detalle de salida: " + error.getMessage(), error));
            }).handle((result, sink) -> {
                if (result == null || result.getId_detalle_orden() == null) {
                    log.error("El detalle de salida para artículo {} se guardó pero no se generó un ID válido",
                            entity.getId_articulo());
                    sink.error(new RuntimeException("El detalle de salida se guardó pero no se generó un ID válido"));
                } else {
                    sink.next(result);
                }
            });
    }

    private Flux<DetailSalidaLoteEntity> procesarLotes(DetailSalidaEntity detail_item, List<LoteDTO> lotes) {
        log.debug("Iniciando procesamiento de {} lotes para el detalle de salida ID: {}",
                lotes.size(), detail_item.getId_detalle_orden());

        return Flux.fromIterable(lotes)
            .flatMap(lote -> {
                // Validación de datos
                if (lote.getId_lote() == null) {
                    log.warn("Intento de registrar lote sin ID para el detalle {}", detail_item.getId_detalle_orden());
                    return Flux.error(new RuntimeException("El ID del lote es requerido"));
                }
                if (lote.getCantidad() == null || lote.getCantidad() <= 0) {
                    log.warn("Intento de registrar lote {} con cantidad inválida: {} para el detalle {}",
                            lote.getId_lote(), lote.getCantidad(), detail_item.getId_detalle_orden());
                    return Flux.error(new RuntimeException("La cantidad del lote debe ser mayor a cero"));
                }
                Double montoConsumo = lote.getPrecioUnitario() != null ? lote.getPrecioUnitario() : 0.0;
                Double totalMonto = montoConsumo * lote.getCantidad();
                log.debug("Registrando lote {} con cantidad {}, precio {} para el detalle {}",
                        lote.getId_lote(), lote.getCantidad(), montoConsumo, detail_item.getId_detalle_orden());

                return salidaLoteRepository.agregarDetailSalidaLote(
                    detail_item.getId_detalle_orden().intValue(),
                    lote.getId_lote(),
                    lote.getCantidad(),
                    montoConsumo,
                    totalMonto,
                    detail_item.getId_ordensalida().intValue()
                )
                .doOnSuccess(savedLote -> {
                    log.debug("Lote {} registrado correctamente con ID {} para el detalle {}",
                            lote.getId_lote(), savedLote.getId_salida_lote(), detail_item.getId_detalle_orden());
                })
                .onErrorResume(error -> {
                    log.error("Error al registrar lote {}: {}", lote.getId_lote(), error.getMessage());
                    return Mono.error(new RuntimeException(
                            String.format("Error al registrar lote %d para el detalle %d: %s",
                                    lote.getId_lote(), detail_item.getId_ordensalida(), error.getMessage()), error));
                })
                .doOnNext(savedLote -> {
                    if (savedLote == null || savedLote.getId_salida_lote() == null) {
                        log.error("El lote {} se guardó pero no se generó un ID válido para el detalle {}",
                                lote.getId_lote(), detail_item.getId_detalle_orden());
                        throw new RuntimeException(
                                String.format("El lote %d se guardó pero no se generó un ID válido", lote.getId_lote()));
                    }
                    log.debug("Lote {} registrado correctamente con ID {}", lote.getId_lote(), savedLote.getId_salida_lote());
                });
            })
            .onErrorContinue((error, obj) -> {
                // Aquí puedes decidir si continuar procesando otros lotes o no
                // onErrorContinue permitirá que el flujo continúe con los siguientes elementos
                // a pesar de que alguno falle
                log.warn("Error al procesar un lote pero continuando con los demás: {}", error.getMessage());
            })
            .doOnComplete(() -> log.info("Procesamiento de lotes completado para el detalle {}", detail_item.getId_detalle_orden()))
            .doOnCancel(() -> {
                log.warn("Procesamiento de lotes cancelado para el detalle {}", detail_item.getId_detalle_orden());
            });
    }

}
