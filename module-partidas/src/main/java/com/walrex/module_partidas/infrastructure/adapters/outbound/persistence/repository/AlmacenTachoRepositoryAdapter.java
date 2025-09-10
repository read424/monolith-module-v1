package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.AlmacenTachoProjection;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de repositorio reactivo para Almacen Tacho usando DatabaseClient
 * Reemplaza las anotaciones @Query con consultas programáticas más flexibles
 * Mantiene la misma funcionalidad que AlmacenTachoRepository pero con mayor control
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlmacenTachoRepositoryAdapter {

    private final DatabaseClient databaseClient;

    /**
     * Consulta principal de almacén tacho con paginación
     * Incluye múltiples JOINs y campos calculados
     * 
     * @param idAlmacen ID del almacén
     * @param limit     Límite de registros
     * @param offset    Offset para paginación
     * @return Flux de proyecciones AlmacenTachoProjection
     */
    public Flux<AlmacenTachoProjection> findAlmacenTachoByAlmacenId(Integer idAlmacen, Integer limit, Integer offset) {
        log.debug("🔍 Consultando almacén tacho con DatabaseClient - Almacén: {}, Limit: {}, Offset: {}", 
                 idAlmacen, limit, offset);

        String sql = buildBaseQuery() + """
                WHERE o.status = 1 AND o.id_almacen = :idAlmacen
                GROUP BY o.id_ordeningreso, d.id_detordeningreso, tp.id_partida, t.id_cliente, tr.id_receta, tc.id_colores, tt.id_tenido, tg.id_gama
                ORDER BY o.id_ordeningreso DESC
                LIMIT :limit OFFSET :offset
                """;

        return databaseClient.sql(sql)
                .bind("idAlmacen", idAlmacen)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, metadata) -> mapToAlmacenTachoProjection(row))
                .all()
                .doOnNext(projection -> log.debug("✅ Proyección mapeada - ID Orden: {}, Partida: {}", 
                         projection.getIdOrdeningreso(), projection.getCodPartida()))
                .doOnComplete(() -> log.info("📊 Consulta completada para almacén ID: {}", idAlmacen))
                .doOnError(error -> log.error("❌ Error en consulta almacén ID {}: {}", idAlmacen, error.getMessage()));
    }

    /**
     * Consulta de almacén tacho sin paginación
     * 
     * @param idAlmacen ID del almacén
     * @return Flux de proyecciones AlmacenTachoProjection
     */
    public Flux<AlmacenTachoProjection> findAlmacenTachoByAlmacenId(Integer idAlmacen) {
        log.debug("🔍 Consultando almacén tacho sin paginación - Almacén: {}", idAlmacen);

        String sql = buildBaseQuery() + """
                WHERE o.status = 1 AND o.id_almacen = :idAlmacen
                GROUP BY o.id_ordeningreso, d.id_detordeningreso, tp.id_partida, t.id_cliente, tr.id_receta, tc.id_colores, tt.id_tenido, tg.id_gama
                ORDER BY o.id_ordeningreso DESC
                """;

        return databaseClient.sql(sql)
                .bind("idAlmacen", idAlmacen)
                .map((row, metadata) -> mapToAlmacenTachoProjection(row))
                .all()
                .doOnComplete(() -> log.info("📊 Consulta sin paginación completada para almacén ID: {}", idAlmacen));
    }

    /**
     * Consulta de almacén tacho con búsqueda por código de partida
     * Incluye múltiples JOINs y campos calculados con filtro de búsqueda
     * 
     * @param idAlmacen  ID del almacén
     * @param codPartida Código de partida para búsqueda (opcional)
     * @param limit      Límite de registros
     * @param offset     Offset para paginación
     * @return Flux de proyecciones AlmacenTachoProjection
     */
    public Flux<AlmacenTachoProjection> findAlmacenTachoByAlmacenIdAndCodPartida(Integer idAlmacen, String codPartida,
                                                                                Integer limit, Integer offset) {
        log.debug("🔍 Consultando almacén tacho con filtro - Almacén: {}, Partida: {}, Limit: {}, Offset: {}", 
                 idAlmacen, codPartida, limit, offset);

        String sql = buildBaseQuery() + """
                WHERE o.status = 1 AND o.id_almacen = :idAlmacen
                AND (:codPartida IS NULL OR (tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END) LIKE '%' || :codPartida || '%')
                GROUP BY o.id_ordeningreso, d.id_detordeningreso, tp.id_partida, t.id_cliente, tr.id_receta, tc.id_colores, tt.id_tenido, tg.id_gama
                ORDER BY o.id_ordeningreso DESC
                LIMIT :limit OFFSET :offset
                """;

        var query = databaseClient.sql(sql)
                .bind("idAlmacen", idAlmacen)
                .bind("limit", limit)
                .bind("offset", offset);

        // Bind condicional para codPartida
        if (codPartida != null && !codPartida.trim().isEmpty()) {
            query = query.bind("codPartida", codPartida.trim());
        } else {
            query = query.bindNull("codPartida", String.class);
        }

        return query.map((row, metadata) -> mapToAlmacenTachoProjection(row))
                .all()
                .doOnNext(projection -> log.debug("✅ Proyección filtrada mapeada - Partida: {}", projection.getCodPartida()))
                .doOnComplete(() -> log.info("📊 Consulta con filtro completada - Almacén: {}, Partida: {}", idAlmacen, codPartida));
    }

    public Mono<Integer> countAlmacenTachoByAlmacenId(Integer idAlmacen, String codPartida) {
        log.debug("🔍 Contando almacén tacho con filtro - Almacén: {}, Partida: {}", idAlmacen, codPartida);
        
        // 📝 1. Lista para almacenar las condiciones SQL (filtros)
        List<String> filtros = new ArrayList<>();

        // 📝 2. Mapa para almacenar los parámetros (key = nombre, value = valor)
        Map<String, Object> parametros = new HashMap<>();

        // 🔍 3. Agregando filtros dinámicamente
        if(idAlmacen != null && idAlmacen > 0) {
            filtros.add("o.id_almacen = :idAlmacen");
            parametros.put("idAlmacen", idAlmacen);
        }

        if(codPartida != null && !codPartida.trim().isEmpty()) {
            filtros.add("(tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END) LIKE '%' || :codPartida || '%'");
            parametros.put("codPartida", codPartida);
        }

        StringBuilder sqlBuilder = new StringBuilder();

        // 🔗 4. agregar la query de conteo
        sqlBuilder.append(buildBaseCountQuery());

        if(filtros.size() > 0) {
            sqlBuilder.append(" WHERE ").append(String.join(" AND ", filtros));
        }

        String sql = sqlBuilder.toString();
        log.debug("SQL Count generado: {}", sql);

        return databaseClient.sql(sql)
                .bindValues(parametros)
                .fetch()
                .one()
                .map(row -> {
                    Object value = row.get("total_rows");
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    return 0;
                })
                .defaultIfEmpty(0)
                .doOnNext(total -> log.debug("✅ Total de registros encontrados: {}", total))
                .doOnError(error -> log.error("❌ Error contando registros: {}", error.getMessage()));
    }

    /**
     * Construye la consulta SQL base compartida por todos los métodos
     * Evita duplicación de código y facilita mantenimiento
     * 
     * @return String con la consulta SQL base
     */
    private String buildBaseQuery() {
        return """
                SELECT o.id_ordeningreso, o.id_cliente
                , CASE WHEN t.id_tipodoc=3 THEN t.no_razon ELSE TRIM(t.no_apepat || t.no_apemat ||', '|| t.no_nombres) END AS razon_social
                , t.no_alias
                , o.fec_registro, o.cod_ingreso, d.id_detordeningreso
                , tp.id_partida
                , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso::varchar END AS cod_partida
                , COUNT(dp.cod_rollo) AS cnt_rollos, tr.cod_receta, tc.no_colores, tc.id_tipo_tenido, tt.desc_tenido, tg.no_gama
                FROM almacenes.ordeningreso o
                INNER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
                INNER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningreso = d.id_detordeningreso AND dp.status = 1
                LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = d.id_comprobante
                LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = tp.id_cliente
                LEFT OUTER JOIN laboratorio.tb_receta tr ON tr.id_receta = tp.id_receta
                LEFT OUTER JOIN laboratorio.tbcolores tc ON tc.id_colores = tr.id_colores
                LEFT OUTER JOIN laboratorio.tbgamas tg ON tg.id_gama = tc.id_gama
                LEFT OUTER JOIN laboratorio.tbtenido tt ON tt.id_tenido = tc.id_tipo_tenido
                """;
    }

    private String buildBaseCountQuery() {
        return """
                SELECT COUNT(DISTINCT o.id_ordeningreso) AS total_rows
                FROM almacenes.ordeningreso o
                INNER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
                INNER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningreso = d.id_detordeningreso AND dp.status = 1
                LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = d.id_comprobante
                LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = tp.id_cliente
                LEFT OUTER JOIN laboratorio.tb_receta tr ON tr.id_receta = tp.id_receta
                LEFT OUTER JOIN laboratorio.tbcolores tc ON tc.id_colores = tr.id_colores
                LEFT OUTER JOIN laboratorio.tbgamas tg ON tg.id_gama = tc.id_gama
                LEFT OUTER JOIN laboratorio.tbtenido tt ON tt.id_tenido = tc.id_tipo_tenido
                """;
    }

    /**
     * Mapea una fila de resultado SQL a una proyección AlmacenTachoProjection
     * Manejo seguro de valores nulos y tipos de datos
     * 
     * @param row Fila de resultado de la consulta SQL
     * @return AlmacenTachoProjection mapeada
     */
    private AlmacenTachoProjection mapToAlmacenTachoProjection(Row row) {
        try {
            return AlmacenTachoProjection.builder()
                    .idOrdeningreso(row.get("id_ordeningreso", Integer.class))
                    .idCliente(row.get("id_cliente", Integer.class))
                    .razonSocial(row.get("razon_social", String.class))
                    .noAlias(row.get("no_alias", String.class))
                    .fecRegistro(row.get("fec_registro", LocalDateTime.class))
                    .codIngreso(row.get("cod_ingreso", String.class))
                    .idDetordeningreso(row.get("id_detordeningreso", Integer.class))
                    .idPartida(row.get("id_partida", Integer.class))
                    .codPartida(row.get("cod_partida", String.class))
                    .cntRollos(row.get("cnt_rollos", Integer.class))
                    .codReceta(row.get("cod_receta", String.class))
                    .noColores(row.get("no_colores", String.class))
                    .idTipoTenido(row.get("id_tipo_tenido", Integer.class))
                    .descTenido(row.get("desc_tenido", String.class))
                    .noGama(row.get("no_gama", String.class))
                    .build();
        } catch (Exception e) {
            log.error("❌ Error mapeando fila a AlmacenTachoProjection: {}", e.getMessage());
            throw new RuntimeException("Error en mapeo de proyección", e);
        }
    }
}
