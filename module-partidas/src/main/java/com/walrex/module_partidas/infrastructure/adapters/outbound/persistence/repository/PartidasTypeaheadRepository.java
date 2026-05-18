package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaTypeaheadItem;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidasTypeaheadRepository {

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    public Mono<PagedResponse<PartidaTypeaheadItem>> listPartidas(String search, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int offset = safePage * safeSize;
        String trimmedSearch = search == null ? "" : search.trim();

        return Mono.zip(
                findPartidas(trimmedSearch, safeSize, offset).collectList(),
                countPartidas(trimmedSearch)
        ).map(tuple -> PagedResponse.of(tuple.getT1(), safePage, safeSize, tuple.getT2()));
    }

    private reactor.core.publisher.Flux<PartidaTypeaheadItem> findPartidas(String search, int limit, int offset) {
        String sql = """
                SELECT tp.id_partida,
                       tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE 'R-' || tp.num_reproceso::varchar END AS cod_partida,
                       tp.id_cliente,
                       tp.id_receta,
                       tp.cnt_rollo,
                       tp.total_kg,
                       tp.fec_entrega,
                       tp.curva_diseno::text AS curva_diseno,
                       CASE
                           WHEN cli.id_tipodoc = 3 THEN cli.no_razon
                           ELSE TRIM(FROM cli.no_apepat || ' ' || cli.no_apemat) || ', ' || cli.no_nombres
                       END AS razon_social,
                       cli.no_alias,
                       rec.cod_receta,
                       rec.desc_receta
                FROM produccion.tb_partidas tp
                LEFT JOIN comercial.tbclientes cli ON cli.id_cliente = tp.id_cliente
                LEFT JOIN laboratorio.tb_receta rec ON rec.id_receta = tp.id_receta
                WHERE tp.condition = 1
                  AND (
                      :search = ''
                      OR (tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE 'R-' || tp.num_reproceso::varchar END) ILIKE '%' || :search || '%'
                      OR CASE
                            WHEN cli.id_tipodoc = 3 THEN cli.no_razon
                            ELSE TRIM(FROM cli.no_apepat || ' ' || cli.no_apemat) || ', ' || cli.no_nombres
                         END ILIKE '%' || :search || '%'
                      OR COALESCE(cli.no_alias, '') ILIKE '%' || :search || '%'
                      OR COALESCE(rec.cod_receta, '') ILIKE '%' || :search || '%'
                      OR COALESCE(rec.desc_receta, '') ILIKE '%' || :search || '%'
                  )
                ORDER BY tp.id_partida DESC
                LIMIT :limit OFFSET :offset
                """;

        return databaseClient.sql(sql)
                .bind("search", search)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, metadata) -> mapRow(row))
                .all();
    }

    private Mono<Long> countPartidas(String search) {
        String sql = """
                SELECT COUNT(*) AS total_rows
                FROM produccion.tb_partidas tp
                LEFT JOIN comercial.tbclientes cli ON cli.id_cliente = tp.id_cliente
                LEFT JOIN laboratorio.tb_receta rec ON rec.id_receta = tp.id_receta
                WHERE tp.condition = 1
                  AND (
                      :search = ''
                      OR (tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE 'R-' || tp.num_reproceso::varchar END) ILIKE '%' || :search || '%'
                      OR CASE
                            WHEN cli.id_tipodoc = 3 THEN cli.no_razon
                            ELSE TRIM(FROM cli.no_apepat || ' ' || cli.no_apemat) || ', ' || cli.no_nombres
                         END ILIKE '%' || :search || '%'
                      OR COALESCE(cli.no_alias, '') ILIKE '%' || :search || '%'
                      OR COALESCE(rec.cod_receta, '') ILIKE '%' || :search || '%'
                      OR COALESCE(rec.desc_receta, '') ILIKE '%' || :search || '%'
                  )
                """;

        return databaseClient.sql(sql)
                .bind("search", search)
                .fetch()
                .one()
                .map(row -> {
                    Object value = row.get("total_rows");
                    return value instanceof Number ? ((Number) value).longValue() : 0L;
                })
                .defaultIfEmpty(0L);
    }

    private PartidaTypeaheadItem mapRow(Row row) {
        return PartidaTypeaheadItem.builder()
                .idPartida(row.get("id_partida", Integer.class))
                .codPartida(row.get("cod_partida", String.class))
                .idCliente(row.get("id_cliente", Integer.class))
                .idReceta(row.get("id_receta", Integer.class))
                .cntRollo(row.get("cnt_rollo", Integer.class))
                .totalKg(row.get("total_kg", Double.class))
                .fecEntrega(row.get("fec_entrega", LocalDate.class))
                .curvaDiseno(parseJson(row.get("curva_diseno", String.class)))
                .razonSocial(row.get("razon_social", String.class))
                .noAlias(row.get("no_alias", String.class))
                .codReceta(row.get("cod_receta", String.class))
                .descReceta(row.get("desc_receta", String.class))
                .build();
    }

    private JsonNode parseJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            log.warn("No se pudo parsear curva_diseno de partida: {}", ex.getMessage());
            return objectMapper.valueToTree(Map.of("raw", json));
        }
    }
}
