package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.KardexRepositoryPort;
import com.walrex.module_almacen.common.utils.RowMapperHelper;
import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleEnriquecido;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.KardexDetalleMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.KardexEnriquecidoMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.DocMovimientoIngresoKardex;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.DocumentoMovimientoEgresoKardex;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.KardexDetalleProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.LoteMovimientoIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KardexRepositoryAdapter implements KardexRepositoryPort {
    private final DatabaseClient databaseClient;
    private final OrdenIngresoRepository ordenIngresoRepository;
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final RowMapperHelper rowMapperHelper;
    private final KardexEnriquecidoMapper kardexEnriquecidoMapper;
    private final KardexDetalleMapper kardexDetalleMapper;
    private final R2dbcEntityTemplate r2dbcTemplate;

    @Override
    public Mono<List<KardexArticuloDTO>> consultarMovimientosKardex(CriteriosBusquedaKardex criterios) {

        return consultarKardexBase(criterios)
                .flatMap(this::enriquecerRegistrosKardexOptimizado)
                .map(this::agruparPorArticulo);
    }

    private Mono<List<KardexDetalleProjection>> consultarKardexBase(CriteriosBusquedaKardex criterios) {
        String query = buildDynamicQuery(criterios);

        var spec = databaseClient.sql(query);
        spec = bindParameters(spec, criterios);

        return spec.map((row, metadata) -> mapRowToProjection(row))
                .all()
                .collectList()
                .doOnNext(results -> {
                    log.info("📊 registros encontrados {} ", results.size());
                });
    }

    /**
     * Enriquece todos los registros de kardex de forma optimizada,
     * evitando consultas duplicadas usando cachés locales
     */
    private Mono<List<KardexDetalleEnriquecido>> enriquecerRegistrosKardexOptimizado(List<KardexDetalleProjection> registrosKardex) {
        // Cachés locales para documentos ya consultados
        Map<Integer, DocMovimientoIngresoKardex> cacheIngresos = new ConcurrentHashMap<>();
        Map<Integer, DocumentoMovimientoEgresoKardex> cacheSalidas = new ConcurrentHashMap<>();
        Map<Integer, LoteMovimientoIngreso> cacheLotes = new ConcurrentHashMap<>();

        return Flux.fromIterable(registrosKardex)
                .flatMap(kardex -> enriquecerRegistroKardexConCache(kardex, cacheIngresos, cacheSalidas, cacheLotes))
                .collectList();
    }

    /**
     * Enriquece un registro individual usando cachés para evitar consultas duplicadas
     */
    private Mono<KardexDetalleEnriquecido> enriquecerRegistroKardexConCache(
            KardexDetalleProjection kardex,
            Map<Integer, DocMovimientoIngresoKardex> cacheIngresos,
            Map<Integer, DocumentoMovimientoEgresoKardex> cacheSalidas,
            Map<Integer, LoteMovimientoIngreso> cacheLotes
            ) {

        KardexDetalleEnriquecido enriquecido = kardexEnriquecidoMapper.toEnriquecido(kardex);
        return switch (kardex.getTipoKardex()) {
            case 1 -> consultarIngresoConCache(kardex.getIdLote(), cacheIngresos)
                    .doOnNext(detalle -> {
                        enriquecido.setCodigoDocumento(detalle.getCod_ingreso());
                        enriquecido.setDescDocumentoIngreso(String.format(" %s %s", detalle.getNo_motivo(), detalle.getCod_ingreso()));
                    })
                    .thenReturn(enriquecido);
            case 2 -> consultarSalidaConCache(kardex.getIdDocumento(), cacheSalidas)
                    .flatMap(detalleEgreso->{
                        enriquecido.setCodigoDocumento(detalleEgreso.getCod_egreso());
                        return obtenerDescripcionLote(kardex.getIdLote(), cacheIngresos, cacheLotes, enriquecido);
                    })
                    .onErrorReturn(enriquecido);
            default -> {
                enriquecido.setCodigoDocumento(null);
                enriquecido.setDescDocumentoIngreso(null);
                yield Mono.just(enriquecido);
            }
        };
    }

    /**
     * Consulta ingreso segun id_lote con caché local
     */
    private Mono<LoteMovimientoIngreso> consultarLoteIngresoConCache(
            Integer idLote,
            Map<Integer, LoteMovimientoIngreso> cache) {

        // Si ya está en caché, devuelve directamente
        if (cache.containsKey(idLote)) {
            return Mono.just(cache.get(idLote));
        }

        String query = """
            SELECT det_inv.id_lote, ord_ing.id_ordeningreso, det_inv.id_detordeningreso, ord_ing.cod_ingreso, mot.no_motivo 
            FROM almacenes.detalle_inventario AS det_inv 
            LEFT OUTER JOIN almacenes.detordeningreso AS det_ing ON det_ing.id_detordeningreso=det_inv.id_detordeningreso 
            LEFT OUTER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso=det_ing.id_ordeningreso 
            LEFT OUTER JOIN almacenes.tbmotivos AS mot ON mot.id_motivo=ord_ing.id_motivo 
            WHERE det_inv.id_lote=:idLote
            """;

        return r2dbcTemplate.getDatabaseClient()
                .sql(query)
                .bind("idLote", idLote)
                .map(row -> LoteMovimientoIngreso.builder()
                            .id_lote(row.get("id_lote", Integer.class))
                            .id_ordeningreso(row.get("id_ordeningreso", Integer.class))
                            .id_detordeningreso(row.get("id_detordeningreso", Integer.class))
                            .cod_ingreso(row.get("cod_ingreso", String.class))
                            .no_motivo(row.get("no_motivo", String.class))
                            .build())
                .one()
                .doOnNext(detalle -> cache.put(idLote, detalle));
    }

    /**
     * Consulta detalle de ingreso con caché local
     */
    private Mono<DocMovimientoIngresoKardex> consultarIngresoConCache(
            Integer idLote,
            Map<Integer, DocMovimientoIngresoKardex> cache) {

        if (cache.containsKey(idLote)) {
            return Mono.just(cache.get(idLote));
        }

        String query = """
            SELECT det_inv.id_lote, ord_ing.id_ordeningreso, ord_ing.cod_ingreso, ord_ing.fec_ingreso, mot.no_motivo 
            FROM almacenes.detalle_inventario AS det_inv
            INNER JOIN almacenes.detordeningreso AS det_ing ON det_ing.id_detordeningreso=det_inv.id_detordeningreso AND det_ing.status=1
            INNER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso=det_ing.id_ordeningreso AND ord_ing.status=1 AND ord_ing.condicion=1
            LEFT OUTER JOIN almacenes.tbmotivos AS mot ON mot.id_motivo=ord_ing.id_motivo
            WHERE det_inv.id_lote=:idLote
            """;

        return r2dbcTemplate.getDatabaseClient()
                .sql(query)
                .bind("idLote", idLote)
                .map(row -> DocMovimientoIngresoKardex.builder()
                        .id_lote(row.get("id_lote", Integer.class))
                        .id_ordeningreso(row.get("id_ordeningreso", Integer.class))
                        .cod_ingreso(row.get("cod_ingreso", String.class))
                        .fec_ingreso(row.get("fec_ingreso", LocalDate.class))
                        .no_motivo(row.get("no_motivo", String.class))
                        .build())
                .one()
                .doOnNext(detalle -> cache.put(idLote, detalle));
    }

    /**
     * Consulta detalle de salida con caché local
     */
    private Mono<DocumentoMovimientoEgresoKardex> consultarSalidaConCache(
            Integer idDocumento,
            Map<Integer, DocumentoMovimientoEgresoKardex> cache) {

        // Si ya está en caché, devuelve directamente
        if (cache.containsKey(idDocumento)) {
            return Mono.just(cache.get(idDocumento));
        }
        String query = """
        SELECT ord_sal.id_ordensalida, ord_sal.cod_salida, ord_sal.fec_entrega, mot.no_motivo
        FROM almacenes.ordensalida AS ord_sal
        LEFT OUTER JOIN almacenes.tbmotivosingresos AS motsal ON motsal.id_motivos_ingreso=ord_sal.id_motivo
        LEFT OUTER JOIN almacenes.tbmotivos AS mot ON mot.id_motivo=motsal.id_motivo
        WHERE ord_sal.status=1 AND ord_sal.id_ordensalida=:idOrdenSalida
        """;

        return r2dbcTemplate.getDatabaseClient()
                .sql(query)
                .bind("idOrdenSalida", idDocumento)
                .map(row -> DocumentoMovimientoEgresoKardex.builder()
                            .id_ordensalida(idDocumento)
                            .cod_egreso(row.get("cod_salida", String.class))
                            .fec_egreso(row.get("fec_entrega", LocalDate.class))
                            .no_motivo(row.get("no_motivo", String.class))
                            .build()
                )
                .one()
                .doOnNext(detalle -> cache.put(idDocumento, detalle))
                .switchIfEmpty(Mono.error(new RuntimeException("Documento salida no encontrado: " + idDocumento)));
    }

    private Mono<KardexDetalleEnriquecido> obtenerDescripcionLote(
            Integer idLote,
            Map<Integer, DocMovimientoIngresoKardex> cacheIngresos,
            Map<Integer, LoteMovimientoIngreso> cacheLotes,
            KardexDetalleEnriquecido enriquecido) {
        if (cacheIngresos.containsKey(idLote)) {
            DocMovimientoIngresoKardex loteInfo = cacheIngresos.get(idLote);
            String descIngreso = String.format("%s %s", loteInfo.getNo_motivo(), loteInfo.getCod_ingreso()).toUpperCase();
            enriquecido.setDescDocumentoIngreso(descIngreso);
            return Mono.just(enriquecido);
        } else {
            return consultarLoteIngresoConCache(idLote, cacheLotes)
                    .doOnNext(loteInfo -> {
                        String descIngreso = String.format("%s %s", loteInfo.getNo_motivo(), loteInfo.getCod_ingreso()).toUpperCase();
                        enriquecido.setDescDocumentoIngreso(descIngreso);
                    })
                    .thenReturn(enriquecido);
        }
    }

    private List<KardexArticuloDTO> agruparPorArticulo(List<KardexDetalleEnriquecido> detallesEnriquecidos) {

        return detallesEnriquecidos.stream()
                .collect(Collectors.groupingBy(KardexDetalleEnriquecido::getIdArticulo))
                .entrySet().stream()
                .map(entry -> construirReporteArticulo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private KardexArticuloDTO construirReporteArticulo(Integer idArticulo, List<KardexDetalleEnriquecido> detalles) {
        // 📊 PRECIO PROMEDIO SOLO DE SALIDAS (tipo_kardex = 2)
        BigDecimal precioPromedio = calcularPrecioPromedioSalidas(detalles);

        // 📦 TOTAL CANTIDAD DE SALIDAS
        BigDecimal totalCantidadSalida = calcularTotalCantidadSalidas(detalles);

        BigDecimal stockDisponible = detalles.getLast().getSaldoStock();

        // 💰 TOTAL VALORIZADO = Precio Promedio × Stock Disponible
        BigDecimal totalValorizado = precioPromedio.multiply(stockDisponible);
        log.debug("💰 Total valorizado: {} × {} = {}", precioPromedio, stockDisponible, totalValorizado);

        List<KardexDetalleDTO> detallesReporte = detalles.stream()
                .map(kardexDetalleMapper::toDTO)
                .collect(Collectors.toList());

        return KardexArticuloDTO.builder()
                .idArticulo(idArticulo)
                .codArticulo(detalles.getFirst().getCodArticulo())
                .descArticulo(detalles.getFirst().getDescArticulo())
                .precioAvg(precioPromedio)
                .totalValorizado(totalValorizado)
                .stockDisponible(stockDisponible)
                .totalCantidadSalida(totalCantidadSalida)
                .detalles(detallesReporte)
                .build();
    }

    private BigDecimal calcularPrecioPromedioSalidas(List<KardexDetalleEnriquecido> detalles) {
        log.debug("📊 === CALCULANDO PRECIO PROMEDIO SOLO SALIDAS ===");

        List<BigDecimal> preciosSalidas = detalles.stream()
                .filter(detalle -> detalle.getTipoKardex() == 2) // Solo salidas
                .map(KardexDetalleEnriquecido::getValorUnidad)
                .filter(Objects::nonNull)
                .filter(precio -> precio.compareTo(BigDecimal.ZERO) > 0) // Solo precios > 0
                .collect(Collectors.toList());

        log.debug("📊 Precios de salidas: {}", preciosSalidas);
        log.debug("📊 Cantidad de salidas para promedio: {}", preciosSalidas.size());

        if (preciosSalidas.isEmpty()) {
            log.warn("📊 No hay precios de salidas válidos para promediar");
            return BigDecimal.ZERO;
        }

        BigDecimal suma = preciosSalidas.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedio = suma.divide(BigDecimal.valueOf(preciosSalidas.size()), 6, RoundingMode.HALF_UP);
        log.debug("📊 Precio promedio salidas: {}", promedio);

        return promedio;
    }

    private BigDecimal calcularTotalCantidadSalidas(List<KardexDetalleEnriquecido> detalles) {
        log.debug("📦 === CALCULANDO TOTAL CANTIDAD SALIDAS ===");

        BigDecimal totalCantidad = detalles.stream()
                .filter(detalle -> detalle.getTipoKardex() == 2) // Solo salidas
                .map(KardexDetalleEnriquecido::getCantidad)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("📦 Total cantidad salidas: {}", totalCantidad);
        return totalCantidad;
    }

    private KardexDetalleProjection mapRowToProjection(Row row) {
        return rowMapperHelper.mapRow(row, KardexDetalleProjection.class);
    }

    private DatabaseClient.GenericExecuteSpec bindParameters(DatabaseClient.GenericExecuteSpec spec, CriteriosBusquedaKardex criterios) {
        if (criterios.getIdArticulo() != null) {
            spec = spec.bind("idArticulo", criterios.getIdArticulo());
        }
        if (criterios.getIdAlmacen() != null) {
            spec = spec.bind("idAlmacen", criterios.getIdAlmacen());
        }
        if (criterios.getFechaInicio() != null) {
            spec = spec.bind("fechaInicio", criterios.getFechaInicio());
        }
        if (criterios.getFechaFin() != null) {
            spec = spec.bind("fechaFin", criterios.getFechaFin());
        }
        return spec;
    }

    private String buildDynamicQuery(CriteriosBusquedaKardex criterios) {
        StringBuilder whereClause = new StringBuilder("WHERE k.tipo_kardex IN (1, 2) ");

        // Agregar condiciones según criterios disponibles
        if (criterios.getIdArticulo() != null) {
            whereClause.append("AND k.id_articulo = :idArticulo ");
        }

        if (criterios.getIdAlmacen() != null) {
            whereClause.append("AND k.id_almacen = :idAlmacen ");
        }

        if (criterios.getFechaInicio() != null && criterios.getFechaFin() != null) {
            whereClause.append("AND k.fecha_movimiento BETWEEN :fechaInicio AND :fechaFin ");
        }

        return String.format(BASE_QUERY_TEMPLATE, whereClause.toString());
    }

    private static final String BASE_QUERY_TEMPLATE = """
       SELECT k.id_kardex, k.tipo_kardex, k.detalle, k.cantidad, k.valor_unidad, k.valor_total
       , k.fecha_movimiento,  k.id_articulo, art.cod_articulo, art.desc_articulo, k.status
       , k.id_unidad, unidad.abrev_unidad, unidad.desc_unidad
       , k.id_unidad_salida, unidad_salida.abrev_unidad AS abrev_salida, unidad_salida.desc_unidad AS desc_unidad_salida 
       , k.id_almacen, k.saldo_stock, k.saldo_lote, k.id_lote
       , k.id_documento, k.id_detalle_documento
       FROM almacenes.kardex AS k
       LEFT OUTER JOIN logistica.tbarticulos AS art ON art.id_articulo=k.id_articulo
       LEFT OUTER JOIN logistica.tbunidad AS unidad ON unidad.id_unidad=k.id_unidad
       LEFT OUTER JOIN logistica.tbunidad AS unidad_salida ON unidad_salida.id_unidad=k.id_unidad_salida
       %s
       ORDER BY k.id_articulo ASC, k.fecha_movimiento ASC
        """;
}
