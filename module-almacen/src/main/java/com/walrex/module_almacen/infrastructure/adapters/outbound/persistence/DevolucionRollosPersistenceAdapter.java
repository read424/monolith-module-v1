package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import java.time.LocalDate;
import java.util.*;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_almacen.application.ports.output.DevolucionRollosPort;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaDevolucionPersistencePort;
import com.walrex.module_almacen.domain.model.OrdenSalidaDevolucionDTO;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ListadoOrdenSalidaDevolucionRequest;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.SalidaDevolucionEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.util.FilterResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para registrar devoluciones de rollos y consultar
 * órdenes de salida
 * Implementa los puertos DevolucionRollosPort y
 * OrdenSalidaDevolucionPersistencePort
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DevolucionRollosPersistenceAdapter implements DevolucionRollosPort, OrdenSalidaDevolucionPersistencePort {

        private final OrdenSalidaRepository ordenSalidaRepository;
        private final DetailSalidaRepository detailSalidaRepository;
        private final DetailOrdenSalidaPesoRepository detailOrdenSalidaPesoRepository;
        private final DevolucionRollosRepository devolucionRollosRepository;
        private final DetalleRolloRepository detalleRolloRepository;
        private final DevolucionServiciosRepository devolucionServiciosRepository;
        private final SalidaDevolucionEntityMapper salidaDevolucionEntityMapper;
        private final DatabaseClient databaseClient;

        @Override
        @Transactional
        public Mono<SalidaDevolucionDTO> registrarDevolucionRollos(SalidaDevolucionDTO devolucionRollos) {
                log.info("🔄 Iniciando registro de devolución en BD - Cliente: {}, Rollos: {}",
                                devolucionRollos.getIdCliente(), devolucionRollos.getArticulos().size());

                return crearOrdenSalida(devolucionRollos)
                                .flatMap(ordenSalida -> procesarRollosPorArticulo(devolucionRollos))
                                .flatMap(this::registrarDevolucionServicios)
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
                                        log.debug("✅ Orden de salida creada - ID: {}", saved.getId());
                                        devolucionRollos.setIdOrdenSalida(saved.getId());
                                })
                                .flatMap(saved -> {
                                        // ✅ Actualizar entregado=1 para disparar trigger que genera código
                                        log.debug("🔄 Actualizando orden para generar código de salida");
                                        return ordenSalidaRepository.updateForGenerateCodigo(saved.getId().intValue())
                                                        .then(ordenSalidaRepository.findById(saved.getId()))
                                                        .doOnNext(ordenActualizada -> {
                                                                log.debug("✅ Código de salida generado: {}",
                                                                                ordenActualizada.getCod_salida());
                                                                devolucionRollos.setCodSalida(
                                                                                ordenActualizada.getCod_salida());
                                                        })
                                                        .switchIfEmpty(Mono.error(new IllegalStateException(
                                                                        "Orden no encontrada después de generar código: "
                                                                                        + saved.getId())));
                                })
                                .onErrorMap(throwable -> {
                                        log.error("❌ Error al crear orden de salida para devolución: {}",
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
                                .doOnNext(articulo -> {
                                        // ✅ Setear idOrdenSalida en cada artículo antes de procesarlo
                                        articulo.setIdOrdenSalida(devolucionRollos.getIdOrdenSalida().intValue());
                                        log.debug("🔄 Artículo preparado - ID: {}, IdOrdenSalida: {}",
                                                        articulo.getIdArticulo(), articulo.getIdOrdenSalida());
                                })
                                .flatMap(this::procesarArticulo)
                                .collectList()
                                .thenReturn(devolucionRollos);
        }

        private Mono<SalidaDevolucionDTO> registrarDevolucionServicios(SalidaDevolucionDTO devolucionRollos) {
                log.debug("🔄 Registrando devolución en tabla devolucion_servicios");

                DevolucionServiciosEntity devolucionServicios = DevolucionServiciosEntity.builder()
                                .idOrdenSalida(devolucionRollos.getIdOrdenSalida().intValue())
                                .idMotivo(devolucionRollos.getIdMotivoDevolucion())
                                .idUsuario(devolucionRollos.getIdUsuario())
                                .build();

                return devolucionServiciosRepository.save(devolucionServicios)
                                .doOnNext(saved -> {
                                        log.debug("✅ Devolución de servicios registrada - ID: {}, OrdenSalida: {}, Motivo: {}, Usuario: {}",
                                                        saved.getId(), saved.getIdOrdenSalida(), saved.getIdMotivo(),
                                                        saved.getIdUsuario());
                                })
                                .thenReturn(devolucionRollos)
                                .onErrorMap(throwable -> {
                                        log.error("❌ Error al registrar devolución de servicios - OrdenSalida: {}, Motivo: {}, Usuario: {}. Error: {}",
                                                        devolucionRollos.getIdOrdenSalida(),
                                                        devolucionRollos.getIdMotivo(),
                                                        devolucionRollos.getIdUsuario(), throwable.getMessage(),
                                                        throwable);

                                        // ✅ Lanzar excepción específica para errores de persistencia
                                        return new RuntimeException(
                                                        "Error al registrar devolución de servicios para orden " +
                                                                        devolucionRollos.getIdOrdenSalida() + ": "
                                                                        + throwable.getMessage(),
                                                        throwable);
                                });
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

                // ✅ DIAGNÓSTICO: Verificar qué rollos llegan
                articulo.getRollos().forEach(rollo -> {
                        log.debug("🔍 Rollo recibido - Código: {}, Selected: {}, Delete: {}",
                                        rollo.getCodRollo(), rollo.getSelected(), rollo.getDelete());
                });

                return Flux.fromIterable(articulo.getRollos())
                                .doOnNext(rollo -> log.debug("🔍 Evaluando rollo: {} - Selected: {}, Delete: {}",
                                                rollo.getCodRollo(), rollo.getSelected(), rollo.getDelete()))
                                .filter(rollo -> {
                                        boolean selected = Boolean.TRUE.equals(rollo.getSelected());
                                        boolean shouldProcess = selected;
                                        log.debug("🔍 Rollo {}: selected={}}, shouldProcess={}",
                                                        rollo, selected, shouldProcess);
                                        return shouldProcess;
                                })
                                .doOnNext(rollo -> {
                                        rollo.setIdOrdenSalida(articulo.getIdOrdenSalida());
                                        rollo.setIdDetalleOrden(articulo.getIdDetOrdenSalida());
                                        log.debug("🔄 Preparando rollo para procesar: {} - OrdenSalida: {}, DetalleOrden: {}",
                                                        rollo.getIdRolloIngreso(), rollo.getIdOrdenSalida(),
                                                        rollo.getIdDetalleOrden());
                                })
                                .flatMap(this::procesarRolloIndividual)
                                .then()
                                .doOnSuccess(v -> log.debug("✅ Todos los rollos válidos del artículo {} procesados",
                                                articulo.getIdArticulo()))
                                .onErrorMap(ex -> {
                                        log.error("❌ Error al procesar rollos del artículo {}: {}",
                                                        articulo.getIdArticulo(), ex.getMessage());
                                        return new RuntimeException(
                                                        "Error al procesar rollos del artículo: " + ex.getMessage());
                                });
        }

        private Mono<Void> procesarRolloIndividual(RolloDevolucionDTO rollo) {
                log.debug("🔄 Procesando rollo individual: {}", rollo);

                // Crear detalle de peso del rollo
                DetailOrdenSalidaPesoEntity detallePeso = salidaDevolucionEntityMapper
                                .toDetailOrdenSalidaPesoEntity(rollo);

                return detailOrdenSalidaPesoRepository.save(detallePeso)
                                .doOnNext(detalleGuardado -> {
                                        rollo.setIdDetOrdenSalidaPeso(detalleGuardado.getId().intValue());
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

        /**
         * Obtiene el listado paginado de órdenes de salida por devolución con filtros
         * dinámicos.
         *
         * Consulta las órdenes de salida que cumplan los siguientes criterios:
         * - Almacén origen: 8
         * - Motivo: 33 (devolución)
         * - Filtros opcionales: nombre cliente, rango de fechas, código de salida,
         * número de guía
         *
         * @param request Request con filtros y parámetros de paginación
         * @return Flux con las órdenes encontradas, ordenadas y paginadas
         */
        @Override
        public Flux<OrdenSalidaDevolucionDTO> obtenerListadoOrdenSalidaDevolucion(
                        ListadoOrdenSalidaDevolucionRequest request) {
                log.info("🔍 Consultando listado paginado de órdenes de salida por devolución con filtros: {}",
                                request);
                // 🔧 Usar método helper
                FilterResult filterResult = buildFilters(request);

                Map<String, Object> parametros = new HashMap<>(filterResult.parametros());
                parametros.put("limit", request.getPagination().getSize().longValue());
                parametros.put("offset", request.getPagination().getOffset().longValue());

                // Construcción dinámica de ordenamiento SQL con ordenamiento limpio
                String orderByClause = buildOrderByClause(request.getPagination().getSortBy(),
                                request.getPagination().getSortDirection());

                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("""
                                SELECT d.id_ordensalida, ords.cod_salida, ords.fec_registro::DATE AS fec_registro,
                                       ords.fec_entrega,
                                       ords.id_cliente,
                                       CASE WHEN tbc.id_tipodoc = 3 THEN tbc.no_razon
                                            ELSE TRIM(tbc.no_apepat || ' ' || tbc.no_apemat) || ', ' || tbc.no_nombres
                                       END AS razon_social,
                                       d.id_comprobante, tip_ser.nu_serie, compr.nro_comprobante,
                                       compr.tctipo_serie, compr.fec_comunicacion
                                FROM almacenes.ordensalida ords
                                INNER JOIN almacenes.devolucion_servicios d ON d.id_ordensalida = ords.id_ordensalida
                                LEFT JOIN comercial.tbclientes tbc ON tbc.id_cliente = ords.id_cliente
                                LEFT JOIN facturacion.tbcomprobantes compr ON compr.id_comprobante = d.id_comprobante
                                LEFT JOIN facturacion.tipo_serie tip_ser ON tip_ser.id_compro = compr.tctipo_serie
                                WHERE ords.id_almacen_origen = 8 AND ords.id_motivo = 33
                                """);

                sqlBuilder.append(filterResult.whereClause());
                sqlBuilder.append("\n").append(orderByClause);
                sqlBuilder.append("\nLIMIT :limit OFFSET :offset");

                String sql = sqlBuilder.toString();

                // ✅ LOGGING DETALLADO para debugging
                log.debug("📝 SQL Final: {}", sql);
                log.debug("🎯 Parámetros finales:");
                parametros.forEach((key, value) -> {
                        log.debug("  - {}: {} ({})", key, value,
                                        value != null ? value.getClass().getSimpleName() : "NULL");
                });
                return databaseClient.sql(sql)
                                .bindValues(parametros)
                                .fetch()
                                .all()
                                .cast(java.util.Map.class)
                                .doOnNext(row -> {
                                        // ✅ Log cada fila para ver qué está llegando
                                        log.debug("📋 Fila recibida: {}", row);
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> typedRow = (Map<String, Object>) row;
                                        typedRow.forEach((key, value) -> {
                                                log.debug("  - {}: {} ({})", key, value,
                                                                value != null ? value.getClass().getSimpleName()
                                                                                : "NULL");
                                        });
                                })
                                .map(this::mapRowToOrdenSalidaDevolucionDTO)
                                .doOnNext(orden -> log.debug("📋 Orden mapeada: {} - Cliente: {}",
                                                orden.getIdOrdenSalida(), orden.getRazonSocial()))
                                .doOnComplete(() -> log.info(
                                                "✅ Consulta paginada de órdenes de salida por devolución completada"))
                                .doOnError(error -> {
                                        log.error("❌ Error al consultar órdenes de salida por devolución: {}",
                                                        error.getMessage());
                                        log.error("❌ Stack trace completo: ", error);
                                        log.error("❌ SQL que falló: {}", sql);
                                        log.error("❌ Parámetros: {}", parametros);
                                });
        }

        /**
         * Cuenta el total de órdenes de salida por devolución que cumplen los filtros
         * especificados.
         *
         * <p>
         * Utiliza los mismos criterios de filtrado que la consulta principal para
         * obtener
         * el total de registros disponibles, necesario para el cálculo de la
         * paginación.
         *
         * @param request Request con filtros de búsqueda
         * @return Mono con el total de órdenes encontradas
         */
        @Override
        public Mono<Long> contarOrdenSalidaDevolucion(ListadoOrdenSalidaDevolucionRequest request) {
                log.info("🔢 Contando total de órdenes de salida por devolución con filtros: {}", request);

                FilterResult filterResult = buildFilters(request);

                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("""
                                SELECT COUNT(*) AS total
                                FROM almacenes.ordensalida ords
                                INNER JOIN almacenes.devolucion_servicios d ON d.id_ordensalida = ords.id_ordensalida
                                LEFT JOIN comercial.tbclientes tbc ON tbc.id_cliente = ords.id_cliente
                                LEFT JOIN facturacion.tbcomprobantes compr ON compr.id_comprobante = d.id_comprobante
                                LEFT JOIN facturacion.tipo_serie tip_ser ON tip_ser.id_compro = compr.tctipo_serie
                                WHERE ords.id_almacen_origen = 8 AND ords.id_motivo = 33
                                """);
                sqlBuilder.append(filterResult.whereClause());

                String sql = sqlBuilder.toString();

                // ✅ LOGGING DETALLADO para debugging
                log.debug("📝 SQL Final (COUNT): {}", sql);
                log.debug("🎯 Parámetros finales (COUNT):");
                filterResult.parametros().forEach((key, value) -> {
                        log.debug("  - {}: {} ({})", key, value,
                                        value != null ? value.getClass().getSimpleName() : "NULL");
                });
                return databaseClient.sql(sql)
                                .bindValues(filterResult.parametros())
                                .fetch()
                                .one()
                                .doOnNext(row -> {
                                        // ✅ Log la fila para ver qué está llegando
                                        log.debug("📊 Fila de conteo recibida: {}", row);
                                        // noinspection unchecked
                                        ((Map<String, Object>) row).forEach((key, value) -> {
                                                log.debug("  - {}: {} ({})", key, value,
                                                                value != null ? value.getClass().getSimpleName()
                                                                                : "NULL");
                                        });
                                })
                                .map(row -> {
                                        // ✅ Extracción segura del count
                                        Object total = row.get("total");
                                        if (total instanceof Number) {
                                                Long count = ((Number) total).longValue();
                                                log.debug("📊 Count extraído: {}", count);
                                                return count;
                                        }
                                        log.warn("⚠️ Valor de count no es Number: {} ({})", total,
                                                        total != null ? total.getClass().getSimpleName() : "NULL");
                                        return 0L;
                                })
                                .doOnSuccess(count -> log.info("✅ Conteo completado: {} órdenes encontradas", count))
                                .doOnError(error -> {
                                        log.error("❌ Error al contar órdenes de salida por devolución: {}",
                                                        error.getMessage());
                                        log.error("❌ Stack trace completo: ", error);
                                        log.error("❌ SQL que falló: {}", sql);
                                        log.error("❌ Parámetros: {}", filterResult.parametros());
                                });
        }

        /**
         * Construye la cláusula ORDER BY de forma dinámica y segura
         */
        private String buildOrderByClause(String sortBy, String sortDirection) {
                // Mapeo de campos válidos para ordenamiento
                String columnName = switch (sortBy) {
                        case "id_ordensalida" -> "ords.id_ordensalida";
                        case "cod_salida" -> "ords.cod_salida";
                        case "fec_entrega" -> "ords.fec_entrega";
                        default -> "ords.id_ordensalida"; // Default fallback
                };

                // Validación de dirección de ordenamiento
                String direction = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";

                return String.format("ORDER BY %s %s", columnName, direction);
        }

        /**
         * Mapea una fila del ResultSet a OrdenSalidaDevolucionDTO
         */
        private OrdenSalidaDevolucionDTO mapRowToOrdenSalidaDevolucionDTO(java.util.Map<String, Object> row) {
                return OrdenSalidaDevolucionDTO.builder()
                                .idOrdenSalida(getValueAsLong(row, "id_ordensalida"))
                                .codigoSalida(getValueAsString(row, "cod_salida"))
                                .fechaRegistro(getValueAsLocalDate(row, "fec_registro"))
                                .fechaEntrega(getValueAsLocalDate(row, "fec_entrega"))
                                .idCliente(getValueAsLong(row, "id_cliente"))
                                .razonSocial(getValueAsString(row, "razon_social"))
                                .idComprobante(getValueAsLong(row, "id_comprobante"))
                                .numeroSerie(getValueAsString(row, "nu_serie"))
                                .numeroComprobante(getValueAsString(row, "nro_comprobante"))
                                .idTipoSerie(getValueAsInteger(row, "tctipo_serie"))
                                .fecComunicacion(getValueAsLocalDate(row, "fec_comunicacion"))
                                .build();
        }

        // ✅ Método helper universal para conversión Long
        private Long getValueAsLong(Map<String, Object> row, String key) {
                Object value = row.get(key);
                if (value == null)
                        return null;

                // ✅ Maneja Integer, Long, BigInteger, etc.
                if (value instanceof Number) {
                        return ((Number) value).longValue();
                }

                log.warn("⚠️ Valor no numérico para '{}': {} ({})", key, value, value.getClass());
                return null;
        }

        private Integer getValueAsInteger(Map<String, Object> row, String key) {
                Object value = row.get(key);
                if (value == null)
                        return null;

                if (value instanceof Number)
                        return ((Number) value).intValue();

                log.warn("⚠️ Valor no numérico para '{}': {} ({})", key, value, value.getClass());
                return null;
        }

        private String getValueAsString(Map<String, Object> row, String key) {
                Object value = row.get(key);
                return value != null ? value.toString() : null;
        }

        private LocalDate getValueAsLocalDate(Map<String, Object> row, String key) {
                Object value = row.get(key);
                if (value == null)
                        return null;

                if (value instanceof LocalDate)
                        return (LocalDate) value;
                if (value instanceof java.sql.Date)
                        return ((java.sql.Date) value).toLocalDate();

                log.warn("⚠️ Tipo inesperado para fecha '{}': {} ({})", key, value, value.getClass());
                return null;
        }

        private FilterResult buildFilters(ListadoOrdenSalidaDevolucionRequest request) {
                // 📝 1. Lista para almacenar las condiciones SQL (filtros)
                List<String> filtros = new ArrayList<>();

                // 📝 2. Mapa para almacenar los parámetros (key = nombre, value = valor)
                Map<String, Object> parametros = new HashMap<>();

                // 🔍 3. Agregando filtros dinámicamente
                if (request.getNombreCliente() != null) {
                        filtros.add("CASE WHEN tbc.id_tipodoc = 3 THEN UPPER(tbc.no_razon) " +
                                        "ELSE UPPER(TRIM(tbc.no_apepat || ' ' || tbc.no_apemat) || ', ' || tbc.no_nombres) "
                                        +
                                        "END LIKE UPPER('%' || :nombreCliente || '%')");
                        parametros.put("nombreCliente", request.getNombreCliente());
                        log.debug("🎯 Filtro nombreCliente agregado: '{}'", request.getNombreCliente());
                }
                if (request.getFechaInicio() != null && request.getFechaFin() != null) {
                        filtros.add("(ords.fec_entrega BETWEEN :fechaInicio AND :fechaFin)");
                        parametros.put("fechaInicio", request.getFechaInicio());
                        parametros.put("fechaFin", request.getFechaFin());
                        log.debug("🎯 Filtro fechas agregado: {} - {}", request.getFechaInicio(),
                                        request.getFechaFin());
                }
                if (request.getCodigoSalida() != null) {
                        filtros.add("UPPER(ords.cod_salida) LIKE UPPER('%' || :codigoSalida || '%')");
                        parametros.put("codigoSalida", request.getCodigoSalida());
                        log.debug("🎯 Filtro codigoSalida agregado: '{}'", request.getCodigoSalida());
                }
                if (request.getNumeroGuia() != null) {
                        filtros.add("UPPER(compr.nro_comprobante) LIKE UPPER('%' || :numeroGuia || '%')");
                        parametros.put("numeroGuia", request.getNumeroGuia());
                        log.debug("🎯 Filtro numeroGuia agregado: '{}'", request.getNumeroGuia());
                }

                // 🔗 4. Construir la cláusula WHERE
                String whereClause = filtros.isEmpty() ? "" : " AND (" + String.join(" AND ", filtros) + ")";

                return new FilterResult(filtros, parametros, whereClause);
        }

}
