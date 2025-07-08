package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_almacen.application.ports.output.DevolucionRollosPort;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.SalidaDevolucionEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para registrar devoluciones de rollos
 * Implementa el puerto DevolucionRollosPort
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DevolucionRollosPersistenceAdapter implements DevolucionRollosPort {

        private final OrdenSalidaRepository ordenSalidaRepository;
        private final DetailSalidaRepository detailSalidaRepository;
        private final DetailOrdenSalidaPesoRepository detailOrdenSalidaPesoRepository;
        private final DevolucionRollosRepository devolucionRollosRepository;
        private final DetalleRolloRepository detalleRolloRepository;
        private final SalidaDevolucionEntityMapper salidaDevolucionEntityMapper;

        @Override
        @Transactional
        public Mono<SalidaDevolucionDTO> registrarDevolucionRollos(SalidaDevolucionDTO devolucionRollos) {
                log.info("🔄 Iniciando registro de devolución en BD - Cliente: {}, Rollos: {}",
                                devolucionRollos.getIdCliente(), devolucionRollos.getArticulos().size());

                return crearOrdenSalida(devolucionRollos)
                                .flatMap(ordenSalida -> procesarRollosPorArticulo(devolucionRollos))
                                .doOnNext(resultado -> log.info("✅ Devolución registrada en BD - ID: {}, Código: {}",
                                                resultado.getIdOrdenSalida(), resultado.getCodSalida()))
                                .doOnError(error -> log.error("❌ Error al registrar devolución en BD: {}",
                                                error.getMessage()));
        }

        @Override
        public Mono<Boolean> verificarRolloYaDevuelto(Integer idDetOrdenIngresoPeso) {
                log.debug("🔍 Verificando si rollo ya fue devuelto - ID: {}", idDetOrdenIngresoPeso);

                return devolucionRollosRepository.existsByIdDetOrdenIngresoPeso(idDetOrdenIngresoPeso)
                                .map(entity -> {
                                        log.debug("✅ Rollo {} YA FUE devuelto anteriormente", idDetOrdenIngresoPeso);
                                        return true; // Existe registro = YA fue devuelto
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                        log.debug("✅ Rollo {} NO FUE devuelto anteriormente", idDetOrdenIngresoPeso);
                                        return Mono.just(false); // No existe registro = NO fue devuelto
                                }))
                                .onErrorMap(throwable -> {
                                        log.error("❌ Error al verificar devolución del rollo {}: {}",
                                                        idDetOrdenIngresoPeso, throwable.getMessage());

                                        // Si es error de múltiples resultados (problema de integridad)
                                        if (throwable.getMessage() != null &&
                                                        (throwable.getMessage().contains("more than one") ||
                                                                        throwable.getMessage().contains("multiple") ||
                                                                        throwable.getMessage().contains("duplicate"))) {
                                                return new IllegalStateException(
                                                                "Inconsistencia en datos: El rollo "
                                                                                + idDetOrdenIngresoPeso +
                                                                                " tiene múltiples registros de devolución. Contacte al administrador.");
                                        }

                                        // Para otros errores, mantener la excepción original
                                        return new RuntimeException(
                                                        "Error al verificar estado de devolución del rollo "
                                                                        + idDetOrdenIngresoPeso,
                                                        throwable);
                                });
        }

        private Mono<OrdenSalidaEntity> crearOrdenSalida(SalidaDevolucionDTO devolucionRollos) {
                log.debug("🔄 Creando orden de salida para devolución");

                OrdenSalidaEntity ordenSalida = salidaDevolucionEntityMapper.toOrdenSalidaEntity(devolucionRollos);

                return ordenSalidaRepository.save(ordenSalida)
                                .doOnNext(saved -> {
                                        log.debug("✅ Orden de salida creada - ID: {}, Código: {}",
                                                        saved.getId(), saved.getCod_salida());

                                        // ✅ Setear el código generado por trigger al DTO original
                                        devolucionRollos.setCodSalida(saved.getCod_salida());
                                        devolucionRollos.setIdOrdenSalida(saved.getId());
                                })
                                .onErrorMap(throwable -> {
                                        log.error("❌ Error al guardar orden de salida para devolución: {}",
                                                        throwable.getMessage(), throwable);

                                        // ✅ Lanzar excepción específica para errores de persistencia
                                        return new RuntimeException(
                                                        "Error al crear orden de salida para devolución: "
                                                                        + throwable.getMessage(),
                                                        throwable);
                                });
        }

        private Mono<SalidaDevolucionDTO> procesarRollosPorArticulo(SalidaDevolucionDTO devolucionRollos) {
                log.debug("🔄 Procesando rollos agrupados por artículo");

                return Flux.fromIterable(devolucionRollos.getArticulos())
                                .flatMap(this::procesarArticulo)
                                .collectList()
                                .thenReturn(devolucionRollos);
        }

        private Mono<DetailSalidaEntity> procesarArticulo(DevolucionArticuloDTO articulo) {
                log.debug("🔄 Procesando artículo {}", articulo);

                // Mappear el articulo a DetailSalidaEntity usando el mapper
                DetailSalidaEntity detalleSalida = salidaDevolucionEntityMapper.toDetailSalidaEntity(articulo);

                return detailSalidaRepository.save(detalleSalida)
                                .doOnNext(detalleGuardado -> {
                                        articulo.setIdDetOrdenSalida(detalleGuardado.getId_detalle_orden().intValue());
                                        log.debug("✅ Detalle de artículo creado - ID: {}, Artículo: {}, Peso: {}",
                                                        detalleGuardado.getId_detalle_orden(),
                                                        detalleGuardado.getId_articulo(),
                                                        detalleGuardado.getTot_kilos());
                                })
                                .flatMap(detalleGuardado -> procesarRollosDelArticulo(articulo)
                                                .then(Mono.just(detalleGuardado)))
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al procesar artículo: {}", ex.getMessage());
                                        return new RuntimeException("Error al procesar artículo: " + ex.getMessage());
                                });
        }

        private Mono<Void> procesarRollosDelArticulo(DevolucionArticuloDTO articulo) {
                log.debug("🔄 Procesando {} rollos del artículo {}",
                                articulo.getRollos().size(), articulo.getIdArticulo());

                return Flux.fromIterable(articulo.getRollos())
                                .doOnNext(rollo -> {
                                        rollo.setIdOrdenSalida(articulo.getIdOrdenSalida());
                                        rollo.setIdDetalleOrden(articulo.getIdDetOrdenSalida());
                                        log.debug("🔄 Preparando rollo: {} - OrdenSalida: {}, DetalleOrden: {}",
                                                        rollo.getIdRolloIngreso(), rollo.getIdOrdenSalida(),
                                                        rollo.getIdDetalleOrden());
                                })
                                .flatMap(this::procesarRolloIndividual)
                                .then()
                                .doOnSuccess(v -> log.debug("✅ Todos los rollos del artículo {} procesados",
                                                articulo.getIdArticulo()))
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al procesar rollos del artículo {}: {}",
                                                        articulo.getIdArticulo(), ex.getMessage());
                                        return new RuntimeException(
                                                        "Error al procesar rollos del artículo: " + ex.getMessage());
                                });
        }

        private Mono<Void> procesarRolloIndividual(RolloDevolucionDTO rollo) {
                log.debug("🔄 Procesando rollo individual: {}", rollo.getCodRollo());

                // Crear detalle de peso del rollo
                DetailOrdenSalidaPesoEntity detallePeso = salidaDevolucionEntityMapper
                                .toDetailOrdenSalidaPesoEntity(rollo);

                return detailOrdenSalidaPesoRepository.save(detallePeso)
                                .doOnNext(detalleGuardado -> {
                                        rollo.setIdDetOrdenIngresoPeso(detalleGuardado.getId().intValue());
                                        log.debug("✅ Detalle de peso guardado - ID: {}, Código: {}, Peso: {}",
                                                        detalleGuardado.getId(), rollo.getCodRollo(),
                                                        rollo.getPesoRollo());
                                })
                                .flatMap(detalleGuardado -> crearTrazabilidadDevolucion(rollo))
                                .then(actualizarStatusRollo(rollo))
                                .doOnSuccess(v -> log.debug("✅ Rollo procesado completamente - Código: {}, Peso: {}",
                                                rollo.getCodRollo(), rollo.getPesoRollo()))
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al procesar rollo {}: {}", rollo.getCodRollo(),
                                                        ex.getMessage());
                                        return new RuntimeException("Error al procesar rollo " + rollo.getCodRollo()
                                                        + ": " + ex.getMessage());
                                });
        }

        private Mono<Void> crearTrazabilidadDevolucion(RolloDevolucionDTO rollo) {
                log.debug("🔄 Creando trazabilidad de devolución para rollo: {}", rollo.getCodRollo());

                DevolucionRollosEntity trazabilidad = salidaDevolucionEntityMapper.toDevolucionRollosEntity(rollo);

                return devolucionRollosRepository.save(trazabilidad)
                                .doOnNext(saved -> log.debug(
                                                "✅ Trazabilidad creada - ID: {}, Rollo ingreso: {}, Rollo salida: {}",
                                                saved.getId(), saved.getIdDetOrdenIngresoPeso(),
                                                saved.getIdDetOrdenSalidaPeso()))
                                .then(actualizarStatusRolloAlmacenCondicional(rollo))
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al procesar trazabilidad devolucion rollo {}: {}",
                                                        rollo.getCodRollo(),
                                                        ex.getMessage());
                                        return new RuntimeException(
                                                        "Error al crear trazabilidad de devolución para rollo "
                                                                        + rollo.getCodRollo()
                                                                        + ": " + ex.getMessage());
                                });
        }

        private Mono<Void> actualizarStatusRollo(RolloDevolucionDTO rollo) {
                log.debug("🔄 Actualizando status del rollo: {}", rollo.getCodRollo());

                return detalleRolloRepository.assignedStatusPorDespachar(rollo.getIdDetOrdenIngresoPeso())
                                .doOnNext(rolloActualizado -> log.debug(
                                                "✅ Status del rollo actualizado - ID: {}, Código: {}",
                                                rolloActualizado.getId(), rollo.getCodRollo()))
                                .then()
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al actualizar status del rollo {}: {}",
                                                        rollo.getCodRollo(), ex.getMessage());
                                        return new RuntimeException("Error al actualizar status del rollo "
                                                        + rollo.getCodRollo() + ": " + ex.getMessage());
                                });
        }

        private Mono<Void> actualizarStatusRolloAlmacenCondicional(RolloDevolucionDTO rollo) {
                // ✅ Validar que idDetOrdenIngresoPesoAlmacen no sea null
                if (rollo.getIdDetOrdenIngresoPesoAlmacen() == null) {
                        log.debug("⚠️  idDetOrdenIngresoPesoAlmacen es null para rollo: {} - Omitiendo actualización",
                                        rollo.getCodRollo());
                        return Mono.empty();
                }

                log.debug("🔄 Actualizando status del rollo almacén: {} - ID: {}",
                                rollo.getCodRollo(), rollo.getIdDetOrdenIngresoPesoAlmacen());

                return detalleRolloRepository.assignedStatusPorDespachar(rollo.getIdDetOrdenIngresoPesoAlmacen())
                                .doOnNext(rolloActualizado -> log.debug(
                                                "✅ Status del rollo almacén actualizado - ID: {}, Código: {}",
                                                rolloActualizado.getId(), rollo.getCodRollo()))
                                .then()
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al actualizar status del rollo almacén {}: {}",
                                                        rollo.getCodRollo(), ex.getMessage());
                                        return new RuntimeException("Error al actualizar status del rollo almacén "
                                                        + rollo.getCodRollo() + ": " + ex.getMessage());
                                });
        }

}