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
    import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.KardexDetalleProjection;
    import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
    import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
    import io.r2dbc.spi.Row;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.data.redis.core.ReactiveRedisTemplate;
    import org.springframework.r2dbc.core.DatabaseClient;
    import org.springframework.stereotype.Component;
    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.security.MessageDigest;
    import java.security.NoSuchAlgorithmException;
    import java.time.Duration;
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
        private final ReactiveRedisTemplate<String, List<KardexArticuloDTO>> redisTemplate;
        private final RowMapperHelper rowMapperHelper;
        private final KardexEnriquecidoMapper kardexEnriquecidoMapper;
        private final KardexDetalleMapper kardexDetalleMapper;

        @Override
        @Cacheable(value = "kardexMovimientos", key = "#root.target.generarHashCriterios(#criterios)")
        public Mono<List<KardexArticuloDTO>> consultarMovimientosKardex(CriteriosBusquedaKardex criterios) {
            log.info("🔍 Consultando movimientos kardex para: {}", criterios);

            String cacheKey = generarHashCriterios(criterios);
            log.debug("🔑 Cache key generado: {}", cacheKey);

            return redisTemplate.opsForValue().get(cacheKey)
                            .doOnNext(result -> log.info("🔴 Redis HIT para key: {}", cacheKey))
                            .switchIfEmpty(
                                ejecutarConsultaCompleta(criterios)
                                        .doOnNext(result -> {
                                            redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(15)).subscribe();
                                        })
                            );
        }

        /**
         * Genera un hash único basado en los criterios de búsqueda para ser usado como clave de caché.
         * Nota: Este método se mantiene público o protegido para que @Cacheable pueda acceder a él.
         * Una buena práctica es hacerlo 'protected' si no necesita ser llamado desde fuera de la clase o sus subclases.
         *
         * @param criterios Criterios de búsqueda.
         * @return Una cadena de hash única.
         */
        private String generarHashCriterios(CriteriosBusquedaKardex criterios) {
            StringBuilder hashBuilder = new StringBuilder("kardex:");

            hashBuilder.append("art:").append(criterios.getIdArticulo() != null ? criterios.getIdArticulo() : "null");
            hashBuilder.append(":alm:").append(criterios.getIdAlmacen() != null ? criterios.getIdAlmacen() : "null");
            hashBuilder.append(":fi:").append(criterios.getFechaInicio() != null ? criterios.getFechaInicio() : "null");
            hashBuilder.append(":ff:").append(criterios.getFechaFin() != null ? criterios.getFechaFin() : "null");

            // Generar hash MD5 del string para tener una clave más limpia
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(hashBuilder.toString().getBytes());
                return "kardex:" + bytesToHex(hashBytes);
            } catch (NoSuchAlgorithmException e) {
                // Fallback: usar el string directo (menos limpio pero funcional)
                return hashBuilder.toString().replaceAll("[^a-zA-Z0-9:]", "_");
            }
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }

        private Mono<List<KardexArticuloDTO>> ejecutarConsultaCompleta(CriteriosBusquedaKardex criterios) {
            return consultarKardexBase(criterios)
                    .flatMap(this::enriquecerRegistrosKardexOptimizado)
                    .map(this::agruparPorArticulo);
        }

        private Mono<List<KardexDetalleProjection>> consultarKardexBase(CriteriosBusquedaKardex criterios) {
            String query = buildDynamicQuery(criterios);
            log.info("🔧 Query construido: {}", query);

            var spec = databaseClient.sql(query);
            spec = bindParameters(spec, criterios);

            return spec.map((row, metadata) -> mapRowToProjection(row))
                    .all()
                    .collectList()
                    .doOnNext(results -> log.info("📊 {} registros encontrados", results.size()));
        }

        /**
         * Enriquece todos los registros de kardex de forma optimizada,
         * evitando consultas duplicadas usando cachés locales
         */
        private Mono<List<KardexDetalleEnriquecido>> enriquecerRegistrosKardexOptimizado(List<KardexDetalleProjection> registrosKardex) {
            // Cachés locales para documentos ya consultados
            Map<Integer, DocMovimientoIngresoKardex> cacheIngresos = new ConcurrentHashMap<>();
            Map<Integer, OrdenSalidaEntity> cacheSalidas = new ConcurrentHashMap<>();

            return Flux.fromIterable(registrosKardex)
                    .flatMap(kardex -> enriquecerRegistroKardexConCache(kardex, cacheIngresos, cacheSalidas))
                    .collectList()
                    .doOnNext(resultados -> {
                        log.info("🚀 Enriquecimiento completado. Consultas de ingreso: {}, consultas de salida: {}",
                                cacheIngresos.size(), cacheSalidas.size());
                    });
        }

        /**
         * Enriquece un registro individual usando cachés para evitar consultas duplicadas
         */
        private Mono<KardexDetalleEnriquecido> enriquecerRegistroKardexConCache(
                KardexDetalleProjection kardex,
                Map<Integer, DocMovimientoIngresoKardex> cacheIngresos,
                Map<Integer, OrdenSalidaEntity> cacheSalidas) {

            KardexDetalleEnriquecido enriquecido = kardexEnriquecidoMapper.toEnriquecido(kardex);

            return switch (kardex.getTipoKardex()) {
                case 1 -> consultarIngresoConCache(kardex.getIdDocumento(), cacheIngresos)
                        .doOnNext(detalle -> {
                            enriquecido.setCodigoDocumento(detalle.getCod_ingreso());
                            enriquecido.setDescDocumentoIngreso(detalle.getNo_motivo());
                        })
                        .thenReturn(enriquecido);

                case 2 -> consultarSalidaConCache(kardex.getIdDocumento(), cacheSalidas)
                        .doOnNext(detalle -> {
                            enriquecido.setCodigoDocumento(detalle.getCod_salida());
                            // Aquí puedes agregar más campos de salida si los necesitas
                        })
                        .thenReturn(enriquecido);

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
                Integer idDocumento,
                Map<Integer, DocMovimientoIngresoKardex> cache) {

            // Si ya está en caché, devuelve directamente
            if (cache.containsKey(idDocumento)) {
                log.debug("💾 Cache hit para ingreso ID: {}", idDocumento);
                return Mono.just(cache.get(idDocumento));
            }

            // Si no está en caché, consulta y almacena
            log.debug("🔍 Cache miss para ingreso ID: {}, consultando BD", idDocumento);
            return ordenIngresoRepository.detalleDocumentoIngreso(idDocumento)
                    .doOnNext(detalle -> cache.put(idDocumento, detalle))
                    .doOnError(error -> log.error("❌ Error consultando ingreso ID: {}", idDocumento, error));
        }

        /**
         * Consulta detalle de salida con caché local
         */
        private Mono<OrdenSalidaEntity> consultarSalidaConCache(
                Integer idDocumento,
                Map<Integer, OrdenSalidaEntity> cache) {

            // Si ya está en caché, devuelve directamente
            if (cache.containsKey(idDocumento)) {
                log.debug("💾 Cache hit para salida ID: {}", idDocumento);
                return Mono.just(cache.get(idDocumento));
            }

            // Si no está en caché, consulta y almacena
            log.debug("🔍 Cache miss para salida ID: {}, consultando BD", idDocumento);
            return ordenSalidaRepository.findById(idDocumento.longValue())
                    .doOnNext(detalle -> cache.put(idDocumento, detalle))
                    .doOnError(error -> log.error("❌ Error consultando salida ID: {}", idDocumento, error));
        }

        private List<KardexArticuloDTO> agruparPorArticulo(List<KardexDetalleEnriquecido> detallesEnriquecidos) {
            return detallesEnriquecidos.stream()
                    .collect(Collectors.groupingBy(KardexDetalleEnriquecido::getIdArticulo))
                    .entrySet().stream()
                    .map(entry -> construirReporteArticulo(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        private KardexArticuloDTO construirReporteArticulo(Integer idArticulo, List<KardexDetalleEnriquecido> detalles) {

            // 1. precioAvg: suma de precios / cantidad de items
            BigDecimal precioPromedio = detalles.stream()
                    .map(KardexDetalleEnriquecido::getValorUnidad)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(detalles.size()), RoundingMode.HALF_UP);

            // 2. totalValorizado: type_kardex 1 suma, type_kardex 2 resta
            BigDecimal totalValorizado = detalles.stream()
                    .map(detalle->{
                        BigDecimal valor = detalle.getValorTotal();
                        return detalle.getTipoKardex()==1? valor: valor.negate();
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal stockDisponible = detalles.get(detalles.size()-1).getSaldoStock();

            List<KardexDetalleDTO> detallesReporte = detalles.stream()
                    .map(kardexDetalleMapper::toDTO)
                    .collect(Collectors.toList());

            return KardexArticuloDTO.builder()
                    .idArticulo(idArticulo)
                    .descArticulo(detalles.get(0).getDescArticulo())
                    .precioAvg(precioPromedio)
                    .totalValorizado(totalValorizado)
                    .stockDisponible(stockDisponible)
                    .detalles(detallesReporte)
                    .build();
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
           , k.fecha_movimiento,  k.id_articulo, art.desc_articulo, k.status
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
