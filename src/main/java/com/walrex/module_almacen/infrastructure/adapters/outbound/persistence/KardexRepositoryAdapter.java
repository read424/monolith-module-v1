package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.KardexRepositoryPort;
import com.walrex.module_almacen.common.utils.RowMapperHelper;
import com.walrex.module_almacen.domain.model.CriteriosBusquedaKardex;
import com.walrex.module_almacen.domain.model.LoteMovimientoKardex;
import com.walrex.module_almacen.domain.model.dto.KardexArticuloDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleDTO;
import com.walrex.module_almacen.domain.model.dto.KardexDetalleEnriquecido;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.KardexDetalleMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.KardexEnriquecidoMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloAlmacenRepository;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KardexRepositoryAdapter implements KardexRepositoryPort {
    private final DatabaseClient databaseClient;
    private final RowMapperHelper rowMapperHelper;
    private final KardexEnriquecidoMapper kardexEnriquecidoMapper;
    private final KardexDetalleMapper kardexDetalleMapper;
    private final R2dbcEntityTemplate r2dbcTemplate;
    private final ArticuloAlmacenRepository articuloAlmacenRepository;

    @Override
    public Mono<List<KardexArticuloDTO>> consultarMovimientosKardex(CriteriosBusquedaKardex criterios) {
        return consultarKardexBase(criterios)
                .flatMap(this::enriquecerRegistrosKardexOptimizado)
                .flatMapMany(this::agruparPorArticulo)
                .collectList();
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


    private Flux<KardexArticuloDTO> agruparPorArticulo(List<KardexDetalleEnriquecido> detallesEnriquecidos) {
        return Flux.fromIterable(detallesEnriquecidos.stream()
                .collect(Collectors.groupingBy((KardexDetalleEnriquecido detalle)->
                        detalle.getIdArticulo()+"-"+detalle.getIdAlmacen()
                ))
                .entrySet())
                .flatMap(entry -> {
                    List<KardexDetalleEnriquecido> detallesOrdenados = entry.getValue().stream()
                            .sorted(Comparator.comparing(KardexDetalleEnriquecido::getFechaMovimiento)
                                    .thenComparing(KardexDetalleEnriquecido::getIdKardex))
                            .collect(Collectors.toList());
                    KardexDetalleEnriquecido primerDetails = detallesOrdenados.getFirst();
                    return construirReporteArticulo(primerDetails.getIdArticulo(), primerDetails.getIdAlmacen(), detallesOrdenados);
                })
                .sort(Comparator.comparing(KardexArticuloDTO::getDescArticulo));
    }

    private Mono<KardexArticuloDTO> construirReporteArticulo(Integer idArticulo, Integer idAlmacen, List<KardexDetalleEnriquecido> detalles) {
        return consultarConfiguracionArticulo(idArticulo, idAlmacen)
                .flatMap(articuloConfig->{
                    if (articuloConfig.getIdArticulo() == null) {
                        throw new IllegalArgumentException(
                                String.format("articuloConfig.getIdArticulo() es null para idArticulo: %d idAlmacen: %d", idArticulo, idAlmacen)
                        );
                    }
                    // 🏗️ CONSTRUIR ArticuloKardex con Map<Integer, LoteMovimientoKardex>
                    return construirArticuloKardexConLotes(idAlmacen, articuloConfig, detalles);
                });
    }

    private Mono<ArticuloInventory> consultarConfiguracionArticulo(Integer idArticulo, Integer idAlmacen) {
        log.debug("🔧 Consultando configuración de artículo {} en almacén {}", idArticulo, idAlmacen);

        return articuloAlmacenRepository.getInfoConversionArticulo(idAlmacen, idArticulo)
                .doOnNext(config -> log.debug("✅ Configuración encontrada: {}", config))
                .switchIfEmpty(Mono.defer(Mono::empty));
    }

    private BigDecimal calcularTotalCantidadSalidas(List<KardexDetalleEnriquecido> detalles) {
        return detalles.stream()
                .filter(detalle -> detalle.getTipoKardex() == 2) // Solo salidas
                .map(KardexDetalleEnriquecido::getCantidad)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<KardexArticuloDTO> construirArticuloKardexConLotes(
            Integer idAlmacen, ArticuloInventory articulo,
            List<KardexDetalleEnriquecido> detalles) {

        // 📦 Construir lotes con ID Integer
        HashMap<Integer, LoteMovimientoKardex> lotesMap = construirLotesMovimiento(articulo, detalles);

        // 💰 CALCULAR VALORIZADO REAL BASADO EN LOTES
        return calcularValorizadoBasadoEnLotes(lotesMap, articulo, idAlmacen)
                .map(totalValorizadoReal->{

                    // 📊 CALCULAR PRECIO PROMEDIO (si hay múltiples lotes)
                    BigDecimal precioPromedioLotes = calcularPrecioPromedioLotes(lotesMap);//

                    // 📦 TOTAL CANTIDAD DE SALIDAS
                    BigDecimal totalCantidadSalida = calcularTotalCantidadSalidas(detalles);

                    BigDecimal stockDisponible = detalles.getLast().getSaldoStock();

                    if(detalles.getLast().getTipoKardex()==2)
                        stockDisponible = detalles.getLast().getSaldoStock().add(detalles.getLast().getCantidad());

                    // 💰 TOTAL VALORIZADO = Precio Promedio × Stock Disponible
                    totalValorizadoReal = precioPromedioLotes.multiply(stockDisponible);
                    log.debug("💰 Total valorizado: {} × {} = {}", precioPromedioLotes, stockDisponible, totalValorizadoReal);

                    List<KardexDetalleDTO> detallesReporte = detalles.stream()
                            .map(kardexDetalleMapper::toDTO)
                            .collect(Collectors.toList());

                    return KardexArticuloDTO.builder()
                            .idArticulo(articulo.getIdArticulo())
                            .codArticulo(detalles.getFirst().getCodArticulo())
                            .descArticulo(detalles.getFirst().getDescArticulo())
                            .precioAvg(precioPromedioLotes)
                            .totalValorizado(totalValorizadoReal)
                            .stockDisponible(stockDisponible)
                            .totalCantidadSalida(totalCantidadSalida)
                            .detalles(detallesReporte)
                            .build();
                });
    }

    private HashMap<Integer, LoteMovimientoKardex> construirLotesMovimiento(ArticuloInventory articulo, List<KardexDetalleEnriquecido> detalles){
        HashMap<Integer, LoteMovimientoKardex> lotes = new HashMap<>();

        detalles.forEach(detalle->{
            Integer idUnidad=detalle.getIdUnidad()!=null?detalle.getIdUnidad():1;
            Integer idUnidadSalida=detalle.getIdUnidadSalida()!=null?detalle.getIdUnidadSalida():1;

            BigDecimal cantidadConvertida=detalle.getCantidad();
            BigDecimal precioConvertido = detalle.getValorUnidad();

            if(!idUnidad.equals(idUnidadSalida)){
                Integer valorConv = articulo.getValorConv();
                if(valorConv!=null){
                    BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, valorConv));
                    cantidadConvertida = detalle.getCantidad().multiply(factorConversion).setScale(4, RoundingMode.HALF_UP);
                    precioConvertido = detalle.getValorUnidad().divide(factorConversion, 6, RoundingMode.HALF_UP);
                }
            }

            Integer idLote = detalle.getIdLote();
            if(!lotes.containsKey(idLote)){
                lotes.put(idLote, LoteMovimientoKardex.builder()
                        .firstIdKardex(detalle.getIdKardex())
                        .idLote(idLote)
                        .cantidad(cantidadConvertida)
                        .cantidadStock(detalle.getSaldoStock())
                        .cantidadLote(detalle.getSaldoLote())
                        .precioVenta(precioConvertido)
                        .totalValorizado(BigDecimal.ZERO)
                        .build());
            }else{
                // Actualizar lote existente
                LoteMovimientoKardex loteExistente = lotes.get(idLote);
                // Sumar cantidad solo si es salida
                if (detalle.getTipoKardex() == 2) {
                    loteExistente.setCantidad(loteExistente.getCantidad().add(cantidadConvertida));
                }
                // Actualizar stock y cantidadLote (último registro)
                loteExistente.setCantidadStock(detalle.getSaldoStock());
                loteExistente.setCantidadLote(detalle.getSaldoLote());
            }
            articulo.setStock(detalle.getSaldoStock());
        });
        return lotes;
    }

    private Mono<BigDecimal> calcularValorizadoBasadoEnLotes(Map<Integer, LoteMovimientoKardex> lotes, ArticuloInventory articulo, Integer idAlmacen){
        BigDecimal stockArticulo = articulo.getStock();
        log.debug("💰 === CALCULANDO VALORIZADO BASADO EN LOTES ===");
        log.debug("💰 Cantidad de lotes: {}", lotes.size());
        log.debug("💰 Articulo {}, Stock del artículo: {}", articulo.getIdArticulo(), stockArticulo);
        BigDecimal valorizado = BigDecimal.ZERO;
        Long firstIdKardex;

        if (lotes.isEmpty()) {
            log.warn("💰 No hay lotes disponibles, valorizado = 0");
            return Mono.just(valorizado);
        }

        // Caso 1: Un solo lote
        Set<Integer> lotesExcluidos;
        if (lotes.size() == 1){
            LoteMovimientoKardex loteUnico = lotes.values().stream().findFirst().orElse(null);
            if (loteUnico.getCantidadStock().equals(loteUnico.getCantidadLote())) {
                valorizado = loteUnico.getPrecioVenta().multiply(loteUnico.getCantidadLote());
                log.debug("💰 Caso 1: Un lote, stock==lote. Valorizado: {} × {} = {}",
                        loteUnico.getPrecioVenta(), loteUnico.getCantidadLote(), valorizado);
                return Mono.just(valorizado);
            }
            log.debug("💰 Caso 1: Un lote, pero stock!=lote. Usando stock del artículo");
            valorizado = loteUnico.getPrecioVenta().multiply(loteUnico.getCantidadLote());
            lotesExcluidos =crearSetLotesExcluidos(lotes);
            firstIdKardex = obtenerMinFirstIdKardex(lotes);
        }else{
            // Caso 2: Múltiples lotes
            log.debug("💰 Caso 2: Múltiples lotes ({})", lotes.size());
            // Sumar todos los saldoLote
            BigDecimal sumaSaldoLotes = lotes.values().stream()
                    .map(LoteMovimientoKardex::getCantidadLote)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.debug("💰 Suma de saldoLotes: {}", sumaSaldoLotes);

            if (sumaSaldoLotes.equals(stockArticulo)) {
                log.debug("💰 Suma lotes == stock artículo. Calculando precio promedio ponderado");
                // Calcular precio promedio ponderado
                BigDecimal sumaValorizada = lotes.values().stream()
                        .filter(lote -> lote.getCantidadLote() != null && lote.getPrecioVenta() != null)
                        .map(lote -> lote.getPrecioVenta().multiply(lote.getCantidadLote()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                log.debug("💰 Suma valorizada total: {}", sumaValorizada);
                return Mono.just(sumaValorizada);
            }
            log.warn("💰 Suma lotes ({}) != stock artículo ({}). Usando precio promedio simple",
                    sumaSaldoLotes, stockArticulo);
            lotesExcluidos =crearSetLotesExcluidos(lotes);
            firstIdKardex= obtenerMinFirstIdKardex(lotes);
        }
        final BigDecimal valorizadoFinal = valorizado;

        return buscarLotesPendientes(articulo.getIdArticulo(), idAlmacen, firstIdKardex, lotesExcluidos)
                .collectList()
                .map(lotesPendientes->{
                    BigDecimal valorizadoPendientes=BigDecimal.ZERO;
                    BigDecimal cantidadPendientes=BigDecimal.ZERO;

                    if(lotesPendientes != null && !lotesPendientes.isEmpty()){
                        valorizadoPendientes = lotesPendientes.stream()
                                .filter(rowLote->rowLote.getCantidadLote()!=null && rowLote.getPrecioVenta()!=null)
                                .map(rowLote->rowLote.getCantidadLote().multiply(rowLote.getPrecioVenta()).setScale(4,RoundingMode.HALF_UP))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        cantidadPendientes = lotesPendientes.stream()
                                .map(LoteMovimientoKardex::getCantidadLote)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        lotesPendientes.forEach(lote ->
                                lotes.putIfAbsent(lote.getIdLote(), lote)
                        );
                    }
                    articulo.setStock(articulo.getStock().add(cantidadPendientes));
                    return valorizadoFinal.add(valorizadoPendientes).setScale(4, RoundingMode.HALF_UP);
                });
    }

    private BigDecimal calcularPrecioPromedioLotes(Map<Integer, LoteMovimientoKardex> lotes){
        if (lotes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> precios = lotes.values().stream()
                .map(LoteMovimientoKardex::getPrecioVenta)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (precios.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal suma = precios.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return suma.divide(BigDecimal.valueOf(precios.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Método para obtener el mínimo firstIdKardex de los lotes
     */
    private Long obtenerMinFirstIdKardex(Map<Integer, LoteMovimientoKardex> lotes) {
        return lotes.values().stream()
                .map(LoteMovimientoKardex::getFirstIdKardex)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(0L);
    }

    private Flux<LoteMovimientoKardex> buscarLotesPendientes(Integer idArticulo, Integer idAlmacen,
             Long firstIdKardex, Set<Integer> lotesExcluidos){
        String baseQuery = """
            WITH lotes_pendientes AS(
                SELECT k.id_articulo, k.id_lote, MIN(k.saldo_lote) AS saldo_lote, MAX(k.id_kardex) AS id_kardex
                , AVG(det_inv.costo_consumo) AS valor_unidad, AVG(det_inv.cantidad) AS cantidad
                FROM almacenes.kardex AS k
                LEFT OUTER JOIN almacenes.detalle_inventario AS det_inv ON det_inv.id_lote=k.id_lote
                WHERE k.id_articulo =:idArticulo AND k.id_almacen =:idAlmacen AND k.id_kardex <:firstIdKardex %s
                GROUP BY k.id_articulo, k.id_lote, k.valor_unidad HAVING MIN(k.saldo_stock)>0
            )SELECT pendientes.*
            FROM lotes_pendientes AS pendientes
            LEFT OUTER JOIN almacenes.kardex AS k ON k.id_kardex= pendientes.id_kardex
            WHERE k.cantidad!=pendientes.saldo_lote
            """;
        String finalQuery;
        if(lotesExcluidos!=null && !lotesExcluidos.isEmpty()){
            String placeholders = lotesExcluidos.stream()
                    .map(id->":lote"+id)
                    .collect(Collectors.joining(","));
            finalQuery=String.format(baseQuery, "AND k.id_lote NOT IN ("+placeholders+")");
        }else{
            finalQuery= String.format(baseQuery, "");
        }
        // Crear el cliente SQL
        DatabaseClient.GenericExecuteSpec executeSpec = r2dbcTemplate.getDatabaseClient()
                .sql(finalQuery)
                .bind("idArticulo", idArticulo)
                .bind("idAlmacen", idAlmacen)
                .bind("firstIdKardex", firstIdKardex);
        // Bind de los parámetros del NOT IN si existen
        if (lotesExcluidos != null && !lotesExcluidos.isEmpty()) {
            for (Integer loteId : lotesExcluidos) {
                log.debug("🔍 - Binding lote{}: {}", loteId, loteId);
                executeSpec = executeSpec.bind("lote" + loteId, loteId);
            }
        }
        return executeSpec
                .map(row -> LoteMovimientoKardex.builder()
                        .firstIdKardex(row.get("id_kardex", Long.class))
                        .idLote(row.get("id_lote", Integer.class))
                        .cantidad(BigDecimal.ZERO)
                        .cantidadStock(row.get("cantidad", BigDecimal.class))
                        .cantidadLote(row.get("saldo_lote", BigDecimal.class))
                        .precioVenta(row.get("valor_unidad", BigDecimal.class))
                        .totalValorizado(BigDecimal.ZERO)
                        .build())
                .all();
    }

    private KardexDetalleProjection mapRowToProjection(Row row) {
        return rowMapperHelper.mapRow(row, KardexDetalleProjection.class);
    }

    private Set<Integer> crearSetLotesExcluidos(Map<Integer, LoteMovimientoKardex> lotes) {
        if (lotes == null || lotes.isEmpty()) {
            return Collections.emptySet();
        }
        return lotes.keySet(); // Directo, ya que el Map tiene Integer como key
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
       ORDER BY k.id_kardex ASC, k.id_almacen, k.id_articulo ASC, k.fecha_movimiento ASC
        """;
}
