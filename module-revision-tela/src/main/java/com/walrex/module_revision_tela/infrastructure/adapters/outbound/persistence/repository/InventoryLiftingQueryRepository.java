package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.FilterResult;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowInventoryLiftingRoll;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowLevantamientoProjection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InventoryLiftingQueryRepository {

    private final DatabaseClient databaseClient;

    public Flux<RowLevantamientoProjection> getLevantamientoByIdPeriodo(Integer idPeriodo){
        log.debug("[Repository] getLevantamientoByIdPeriodo - iniciando con idPeriodo: {}", idPeriodo);
        String sql = """
            WITH levantamiento AS (
            	SELECT
                	l.id_ordeningreso, l.id_detordeningreso,
            	    JSONB_AGG(
            	       JSONB_BUILD_OBJECT(
            	           'id_partida', COALESCE(l.id_partida, 0),
            	           'rollos', l.cnt_rollos,
            	           'id', l.id
            	       )
            	    ) AS detalle_json, COUNT(l.id) AS cnt_lev, SUM(l.cnt_rollos) AS total_rollos
            	FROM revision_crudo.levantamiento l
            	WHERE l.id_periodo = $1
            	GROUP BY l.id_ordeningreso, l.id_detordeningreso
            	ORDER BY l.id_ordeningreso, l.id_detordeningreso
            ),
            rollos_revision AS (
            	SELECT ir.id_ordeningreso, dir.id_detordeningreso, COUNT(drr.id_detordeningresopeso) AS total_rollos
            	FROM revision_crudo.ingreso_revision ir
            	LEFT OUTER JOIN revision_crudo.detail_ingreso_revision dir ON dir.id_revision = ir.id_revision
            	LEFT OUTER JOIN revision_crudo.detail_rollo_revision drr ON drr.id_detail = dir.id_detail
            	WHERE ir.id_periodo = $1
            	GROUP BY ir.id_ordeningreso, dir.id_detordeningreso
            ) SELECT lv.id_ordeningreso, lv.id_detordeningreso, (dj.elem->>'id_partida')::INT AS id_partida, o.nu_comprobante, o.nu_serie, o.fec_ingreso, t.no_alias, d.lote, t2.desc_articulo
            , (dj.elem->>'id')::INT AS id_levantamiento
            , (dj.elem->>'rollos')::INT AS rollos_partida
            , lv.cnt_lev, lv.total_rollos, t.nu_ruc
            FROM levantamiento AS lv
            LEFT OUTER JOIN almacenes.ordeningreso o ON o.id_ordeningreso = lv.id_ordeningreso
            LEFT OUTER JOIN almacenes.detordeningreso d ON d.id_detordeningreso = lv.id_detordeningreso
            LEFT OUTER JOIN logistica.tbarticulos t2 ON t2.id_articulo = d.id_articulo
            LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = o.id_cliente
            LEFT JOIN LATERAL jsonb_array_elements(lv.detalle_json ) AS dj(elem) ON TRUE
            LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = (dj.elem->>'id_partida')::INT
            ORDER BY o.id_ordeningreso
            """;

        return databaseClient.sql(sql)
            .bind(0, idPeriodo)
            .map(this::mapToRowLevantamiento)
            .all()
            .doOnSubscribe(s -> log.debug("[Repository] getLevantamientoByIdPeriodo - ejecutando consulta SQL"))
            .doOnNext(row -> log.trace("[Repository] getLevantamientoByIdPeriodo - fila mapeada: idOrden={}, idDetOrden={}",
                row.getIdOrdeningreso(), row.getIdDetOrdenIngreso()))
            .doOnComplete(() -> log.debug("[Repository] getLevantamientoByIdPeriodo - consulta completada"))
            .doOnError(error -> log.error("[Repository] getLevantamientoByIdPeriodo - ERROR para idPeriodo={}: {} - Clase: {}",
                idPeriodo, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    public Flux<RowInventoryLiftingRoll> getRollsAtInventory(Integer idPeriodo, Integer idDetOrdenIngreso){
        log.debug("[Repository] getRollsAtInventory - iniciando con idPeriodo: {}, idDetOrdenIngreso: {}",
            idPeriodo, idDetOrdenIngreso);
        String sql = """
                SELECT ir.id_ordeningreso, dir.id_detordeningreso, drr.id_detordeningresopeso, COALESCE(drr.id_partida, 0) AS id_partida
                , CASE WHEN drr.as_crudo = 1 OR drr.id_partida IS NULL THEN 1 ELSE 0 END AS as_crudo
                , drr.id, drr.id_levantamiento
                FROM revision_crudo.ingreso_revision ir
                LEFT OUTER JOIN revision_crudo.detail_ingreso_revision dir ON dir.id_revision = ir.id_revision
                LEFT OUTER JOIN revision_crudo.detail_rollo_revision drr ON drr.id_detail = dir.id_detail AND drr.id_levantamiento IS NULL
                WHERE ir.id_periodo = $1 AND drr.id_levantamiento IS NULL AND dir.id_detordeningreso = $2
                ORDER BY 5 DESC, COALESCE(drr.id_partida, 0) ASC
            """;

        return databaseClient.sql(sql)
            .bind(0, idPeriodo)
            .bind(1, idDetOrdenIngreso)
            .map(this::mapToRowRolloRevision)
            .all()
            .doOnSubscribe(s -> log.debug("[Repository] getRollsAtInventory - ejecutando consulta SQL"))
            .doOnNext(row -> log.trace("[Repository] getRollsAtInventory - rollo mapeado: id={}, partida={}",
                row.getId(), row.getId_partida()))
            .doOnComplete(() -> log.debug("[Repository] getRollsAtInventory - consulta completada"))
            .doOnError(error -> log.error("[Repository] getRollsAtInventory - ERROR para idPeriodo={}, idDetOrdenIngreso={}: {} - Clase: {}",
                idPeriodo, idDetOrdenIngreso, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    private RowLevantamientoProjection mapToRowLevantamiento(Row row, RowMetadata metadata) {
        return RowLevantamientoProjection.builder()
            .idOrdeningreso(row.get("id_ordeningreso", Integer.class))
            .idDetOrdenIngreso(row.get("id_detordeningreso", Integer.class))
            .idPartida(row.get("id_partida", Integer.class))
            .numComprobante(row.get("nu_comprobante", String.class))
            .numSerie(row.get("nu_serie", String.class))
            .fecIngreso(row.get("fec_ingreso", LocalDate.class))
            .noAlias(row.get("no_alias", String.class))
            .numLote(row.get("lote", String.class))
            .descArticulo(row.get("desc_articulo", String.class))
            .idLevantamiento(row.get("id_levantamiento", Integer.class))
            .cntRolls(row.get("rollos_partida", Integer.class))
            .cntLevantado(row.get("cnt_lev", Integer.class))
            .totalRolls(row.get("total_rollos", Integer.class))
            .numRuc(row.get("nu_ruc", String.class))
            .build();
    }

    private RowInventoryLiftingRoll mapToRowRolloRevision(Row row, RowMetadata metadata){
        return RowInventoryLiftingRoll.builder()
            .id(row.get("id", Integer.class))
            .id_ordeningreso(row.get("id_ordeningreso", Integer.class))
            .id_detordeningreso(row.get("id_detordeningreso", Integer.class))
            .id_detordeningresopeso(row.get("id_detordeningresopeso", Integer.class))
            .id_partida(row.get("id_partida", Integer.class))
            .as_crudo(row.get("as_crudo", Integer.class))
            .id_levantamiento(row.get("id_levantamiento", Integer.class))
            .build();
    }

    /**
     * Actualiza múltiples rollos con el id_levantamiento especificado
     * @param idList Lista de IDs de rollos a actualizar
     * @param idLevantamiento ID del levantamiento a asignar
     * @return Mono con el número de filas actualizadas
     */
    public Mono<Integer> updateRollosWithLevantamiento(List<Integer> idList, Integer idLevantamiento) {
        log.debug("[Repository] updateRollosWithLevantamiento - iniciando con {} rollos, idLevantamiento: {}",
            idList != null ? idList.size() : 0, idLevantamiento);

        if (idList == null || idList.isEmpty()) {
            log.warn("[Repository] updateRollosWithLevantamiento - Lista de rollos vacía, no se realizará actualización");
            return Mono.just(0);
        }

        // Construir placeholders posicionales para PostgreSQL ($2, $3, $4, ...)
        // $1 será para idLevantamiento
        List<String> placeholderList = new ArrayList<>();
        for (int i = 0; i < idList.size(); i++) {
            placeholderList.add("$" + (i + 2)); // empezamos en $2
        }
        String placeholders = String.join(",", placeholderList);

        String sql = """
            UPDATE revision_crudo.detail_rollo_revision
            SET id_levantamiento = $1
            WHERE id IN (%s)
            AND id_levantamiento IS NULL
            """.formatted(placeholders);

        log.debug("[Repository] updateRollosWithLevantamiento - SQL generado con {} placeholders, idLevantamiento={}",
            idList.size(), idLevantamiento);
        log.trace("[Repository] updateRollosWithLevantamiento - IDs a actualizar: {}", idList);

        var spec = databaseClient.sql(sql)
            .bind(0, idLevantamiento);

        // Bind de cada ID en la lista (índices 1, 2, 3, ... corresponden a $2, $3, $4, ...)
        for (int i = 0; i < idList.size(); i++) {
            spec = spec.bind(i + 1, idList.get(i));
        }

        return spec.fetch()
            .rowsUpdated()
            .map(Long::intValue)
            .doOnSubscribe(s -> log.debug("[Repository] updateRollosWithLevantamiento - ejecutando UPDATE"))
            .doOnSuccess(count ->
                log.info("[Repository] updateRollosWithLevantamiento - Actualizados {} rollos con id_levantamiento: {}",
                    count, idLevantamiento)
            )
            .doOnError(error ->
                log.error("[Repository] updateRollosWithLevantamiento - ERROR actualizando {} rollos con idLevantamiento={}: {} - Clase: {}",
                    idList.size(), idLevantamiento, error.getMessage(), error.getClass().getSimpleName(), error)
            );
    }

    /**
     * Decrementa la cantidad_disponible del levantamiento.
     * Si cantidad_disponible es NULL, se inicializa con cnt_rollos antes de decrementar.
     * La consulta usa GREATEST para evitar valores negativos.
     *
     * @param idLevantamiento ID del levantamiento
     * @param cantidad Cantidad a decrementar
     * @return Mono con el número de filas actualizadas
     */
    public Mono<Integer> decrementarCantidadDisponibleLevantamiento(Integer idLevantamiento, Integer cantidad) {
        log.debug("[Repository] decrementarCantidadDisponibleLevantamiento - idLevantamiento: {}, cantidad: {}",
            idLevantamiento, cantidad);

        if (idLevantamiento == null || cantidad == null || cantidad <= 0) {
            log.warn("[Repository] decrementarCantidadDisponibleLevantamiento - parámetros inválidos: idLevantamiento={}, cantidad={}",
                idLevantamiento, cantidad);
            return Mono.just(0);
        }

        // GREATEST asegura que cantidad_disponible nunca sea menor a 0
        String sql = """
            UPDATE revision_crudo.levantamiento
            SET cantidad_disponible = GREATEST(COALESCE(cantidad_disponible, cnt_rollos) - $1, 0)
            WHERE id = $2
            """;

        return databaseClient.sql(sql)
            .bind(0, cantidad)
            .bind(1, idLevantamiento)
            .fetch()
            .rowsUpdated()
            .map(Long::intValue)
            .doOnSubscribe(s -> log.debug("[Repository] decrementarCantidadDisponibleLevantamiento - ejecutando UPDATE"))
            .doOnSuccess(count ->
                log.info("[Repository] decrementarCantidadDisponibleLevantamiento - Actualizado levantamiento id: {}, decrementado en: {}",
                    idLevantamiento, cantidad)
            )
            .doOnError(error ->
                log.error("[Repository] decrementarCantidadDisponibleLevantamiento - ERROR para idLevantamiento={}, cantidad={}: {} - Clase: {}",
                    idLevantamiento, cantidad, error.getMessage(), error.getClass().getSimpleName(), error)
            );
    }

    private FilterResult buildFilters(Integer idDetOrdenIngreso, Integer idPartida) {
        // 📝 1. Lista para almacenar las condiciones SQL (filtros)
        List<String> filtros = new ArrayList<>();

        // 📝 2. Mapa para almacenar los parámetros (key = nombre, value = valor)
        Map<String, Object> parametros = new HashMap<>();

        if (idDetOrdenIngreso != null) {
            filtros.add("(dir.id_detordeningreso = :idDetOrdenIngreso)");
            parametros.put("idDetOrdenIngreso", idDetOrdenIngreso);
        }
        if (idPartida != null) {
            filtros.add("drr.id_partida = :idPartida");
            parametros.put("idPartida", idPartida);
        }

        // 🔗 4. Construir la cláusula WHERE
        String whereClause = filtros.isEmpty() ? "" : " AND (" + String.join(" AND ", filtros) + ")";

        return new FilterResult(filtros, parametros, whereClause);
    }
}
