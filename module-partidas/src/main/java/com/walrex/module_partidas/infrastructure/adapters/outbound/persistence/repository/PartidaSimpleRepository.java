package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaListItem;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidaSimpleRepository {

    private final DatabaseClient databaseClient;

    private static final String BASE_SELECT = """
            SELECT tp.id_partida
                 , tp.id_tipo_partida
                 , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL
                                          THEN '' ELSE '-R' || tp.num_reproceso::varchar END AS cod_partida
                 , tp.cnt_rollo
                 , tp.total_kg
                 , tp.id_cliente
                 , CASE WHEN t.id_tipodoc = 3 THEN t.no_razon
                        ELSE TRIM(t.no_apepat || t.no_apemat || ', ' || t.no_nombres) END  AS razon_social
                 , t2.desc_articulo
            FROM produccion.tb_partidas tp
            LEFT JOIN comercial.tbclientes t              ON t.id_cliente          = tp.id_cliente
            LEFT JOIN comercial.tborden_produccion tp2    ON tp2.id_ordenproduccion = tp.id_ordenproduccion
            LEFT JOIN logistica.tbarticulos t2            ON t2.id_articulo         = tp2.id_articulo
            WHERE tp."condition" = 1
              AND tp.id_tipo_partida != 3
            """;

    private static final String SEARCH_FILTER =
            "  AND tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R' || tp.num_reproceso::varchar END ILIKE '%' || :search || '%'\n";

    private static final String ORDER_LIMIT =
            "ORDER BY tp.cod_partida, COALESCE(tp.id_partida_parent, tp.id_partida)\nLIMIT :limit OFFSET :offset\n";

    private static final String COUNT_WRAP_START =
            "SELECT COUNT(*) FROM (\n    SELECT tp.id_partida\n    FROM produccion.tb_partidas tp\n    LEFT JOIN comercial.tbclientes t ON t.id_cliente = tp.id_cliente\n    LEFT JOIN comercial.tborden_produccion tp2 ON tp2.id_ordenproduccion = tp.id_ordenproduccion\n    LEFT JOIN logistica.tbarticulos t2 ON t2.id_articulo = tp2.id_articulo\n    WHERE tp.\"condition\" = 1 AND tp.id_tipo_partida != 3\n";

    private static final String COUNT_WRAP_END = ") AS total\n";

    public Mono<PagedResponse<PartidaListItem>> listPartidas(String search, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int offset = safePage * safeSize;

        boolean hasSearch = search != null && !search.isBlank();
        String selectSql = hasSearch ? BASE_SELECT + SEARCH_FILTER + ORDER_LIMIT : BASE_SELECT + ORDER_LIMIT;
        String countSql = hasSearch ? COUNT_WRAP_START + SEARCH_FILTER + COUNT_WRAP_END : COUNT_WRAP_START + COUNT_WRAP_END;

        DatabaseClient.GenericExecuteSpec selectSpec = databaseClient.sql(selectSql)
                .bind("limit", safeSize)
                .bind("offset", offset);

        DatabaseClient.GenericExecuteSpec countSpec = databaseClient.sql(countSql);

        if (hasSearch) {
            selectSpec = selectSpec.bind("search", search.trim());
            countSpec = countSpec.bind("search", search.trim());
        }

        return Mono.zip(
                selectSpec.map((row, meta) -> mapRow(row)).all().collectList(),
                countSpec.fetch().one()
                        .map(row -> {
                            Object val = row.get("count");
                            return val instanceof Number ? ((Number) val).longValue() : 0L;
                        })
                        .defaultIfEmpty(0L)
        ).map(tuple -> PagedResponse.of(tuple.getT1(), safePage, safeSize, tuple.getT2()));
    }

    private PartidaListItem mapRow(Row row) {
        return PartidaListItem.builder()
                .idPartida(row.get("id_partida", Integer.class))
                .idTipoPartida(row.get("id_tipo_partida", Integer.class))
                .codPartida(row.get("cod_partida", String.class))
                .cntRollo(row.get("cnt_rollo", Integer.class))
                .totalKg(row.get("total_kg", Double.class))
                .idCliente(row.get("id_cliente", Integer.class))
                .razonSocial(row.get("razon_social", String.class))
                .descArticulo(row.get("desc_articulo", String.class))
                .build();
    }
}
