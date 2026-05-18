package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.domain.model.PagedResponse;
import com.walrex.module_partidas.domain.model.PartidaProduccion;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidaUbicacionRepository {

    private final DatabaseClient databaseClient;

    // ─── PARTE 1: PPM como tabla principal ────────────────────────────────────

    private static final String BASE_FROM_PPM = """
            FROM produccion.partidas_procesos_maquinas ppm
            LEFT JOIN produccion.tb_partidas tp               ON tp.id_partida          = ppm.id_partida
            LEFT JOIN comercial.tbclientes t                  ON t.id_cliente           = tp.id_cliente
            LEFT JOIN comercial.tborden_produccion tp2        ON tp2.id_ordenproduccion = tp.id_ordenproduccion
            LEFT JOIN logistica.tbarticulos t2                ON t2.id_articulo         = tp2.id_articulo
            LEFT JOIN laboratorio.tb_receta tr                ON tr.id_receta           = tp.id_receta
            LEFT JOIN catalogo.tbmaquina t3                   ON t3.id_maquina          = ppm.id_maquina
            LEFT JOIN almacenes.tb_requerimiento_partidas trp ON trp.id_partida         = tp.id_partida
            LEFT JOIN almacenes.tbrequerimientos t4            ON t4.id_requerimiento    = trp.id_requerimiento
                                                             AND t4.id_comprobante       = 4
                                                             AND CASE WHEN t3.id_ubicacion = 18
                                                                      THEN t4.id_motivo = 15
                                                                      ELSE t4.id_motivo IN (15, 14) END
            LEFT JOIN produccion.declaracion_calidad dc       ON dc.id_partida          = tp.id_partida
                                                             AND dc.fecha_declaracion    = ppm.fec_programacion
                                                             AND dc.status               = 1
            LEFT JOIN almacenes.tb_motivo_rechazo mot_recha   ON mot_recha.id            = dc.id_motivo_rechazo
            WHERE tp.id_tipo_partida != 3 AND ppm.fec_programacion = :fecha
              AND t3.id_ubicacion     = :idUbicacion
            """;

    private static final String SELECT_COLS_PPM = """
            SELECT dc.id
                 , dc.status
                 , dc.nivel_critico
                 , mot_recha.descripcion
                 , dc.is_observado
                 , dc.cnt_rollos                                              AS rollos_declarados
                 , tp.cnt_rollo
                 , ppm.fec_programacion
                 , ppm.id_tipo_maquina
                 , ppm.id_partida
                 , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL
                                          THEN '' ELSE '-R' || tp.num_reproceso::varchar END AS cod_partida
                 , tp.id_cliente
                 , CASE WHEN t.id_tipodoc = 3 THEN t.no_razon
                        ELSE TRIM(t.no_apepat || t.no_apemat || ', ' || t.no_nombres) END   AS razon_social
                 , t2.desc_articulo
                 , tr.cod_receta
                 , tr.desc_receta
                 , t3.desc_maq
                 , COUNT(DISTINCT trp.id_partida)                            AS is_vale
                 , ppm.fec_real_inicio
                 , ppm.fec_real_fin
                 , CASE WHEN ppm.fec_real_fin IS NULL
                          OR ppm.fec_real_inicio IS NULL THEN 0 ELSE 1 END  AS is_declarado
                 , t3.id_maquina
            """;

    private static final String GROUP_BY_PPM = """
            GROUP BY ppm.id_partida_maquina, tp.id_partida, tp.cnt_rollo, t.id_cliente,
                     t2.id_articulo, tr.id_receta, t3.id_maquina,
                     dc.id, dc.status, dc.nivel_critico, mot_recha.descripcion,
                     dc.is_observado, dc.cnt_rollos
            """;

    // ─── PARTE 2: declaracion_calidad como tabla principal (no está en PPM) ──

    private static final String BASE_FROM_DC = """
            FROM produccion.declaracion_calidad dc
            LEFT JOIN produccion.tb_partidas tp          ON tp.id_partida          = dc.id_partida
            LEFT JOIN comercial.tbclientes t             ON t.id_cliente           = tp.id_cliente
            LEFT JOIN comercial.tborden_produccion tp2   ON tp2.id_ordenproduccion = tp.id_ordenproduccion
            LEFT JOIN logistica.tbarticulos t2           ON t2.id_articulo         = tp2.id_articulo
            LEFT JOIN laboratorio.tb_receta tr           ON tr.id_receta           = tp.id_receta
            LEFT JOIN catalogo.tbmaquina t3              ON t3.id_maquina          = dc.id_maquina
            LEFT JOIN almacenes.tb_motivo_rechazo mot_recha ON mot_recha.id        = dc.id_motivo_rechazo
            WHERE dc.id_ubicacion      = :idUbicacion
              AND dc.fecha_declaracion = :fecha
              AND dc.status            = 1
              AND tp.id_tipo_partida  != 3
              AND dc.id_partida NOT IN (
                  SELECT ppm2.id_partida
                  FROM produccion.partidas_procesos_maquinas ppm2
                  JOIN catalogo.tbmaquina tm2 ON tm2.id_maquina = ppm2.id_maquina
                  WHERE ppm2.fec_programacion = :fecha
                    AND tm2.id_ubicacion      = :idUbicacion
              )
            """;

    private static final String SELECT_COLS_DC = """
            SELECT dc.id
                 , dc.status
                 , dc.nivel_critico
                 , mot_recha.descripcion
                 , dc.is_observado
                 , dc.cnt_rollos                                              AS rollos_declarados
                 , tp.cnt_rollo
                 , dc.fecha_declaracion                                       AS fec_programacion
                 , NULL::integer                                              AS id_tipo_maquina
                 , dc.id_partida
                 , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL
                                          THEN '' ELSE '-R' || tp.num_reproceso::varchar END AS cod_partida
                 , tp.id_cliente
                 , CASE WHEN t.id_tipodoc = 3 THEN t.no_razon
                        ELSE TRIM(t.no_apepat || t.no_apemat || ', ' || t.no_nombres) END   AS razon_social
                 , t2.desc_articulo
                 , tr.cod_receta
                 , tr.desc_receta
                 , t3.desc_maq
                 , 0::bigint                                                  AS is_vale
                 , NULL::date                                                 AS fec_real_inicio
                 , NULL::date                                                 AS fec_real_fin
                 , 1                                                          AS is_declarado
                 , dc.id_maquina
            """;

    private static final String SEARCH_FILTER = "  AND tp.cod_partida ILIKE '%' || :search || '%'\n";

    public Mono<PagedResponse<PartidaProduccion>> findByUbicacion(
            Integer idUbicacion, LocalDate fecha, String search, int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int offset   = safePage * safeSize;

        boolean hasSearch = search != null && !search.isBlank();
        String fromPPM = hasSearch ? BASE_FROM_PPM + SEARCH_FILTER : BASE_FROM_PPM;
        String fromDC  = hasSearch ? BASE_FROM_DC  + SEARCH_FILTER : BASE_FROM_DC;

        String ppmPart = SELECT_COLS_PPM + fromPPM + GROUP_BY_PPM;
        String dcPart  = SELECT_COLS_DC  + fromDC;

        String selectSql = "SELECT * FROM (\n" + ppmPart +
                           "UNION ALL\n" + dcPart +
                           ") combined\nORDER BY fec_programacion DESC, id_partida\n" +
                           "LIMIT :limit OFFSET :offset\n";

        String ppmCount = "SELECT DISTINCT ppm.id_partida_maquina " + fromPPM;
        String dcCount  = "SELECT dc.id " + fromDC;
        String countSql = "SELECT COUNT(*) FROM (\n" + ppmCount +
                          "\nUNION ALL\n" + dcCount + "\n) total\n";

        DatabaseClient.GenericExecuteSpec selectSpec = databaseClient.sql(selectSql)
                .bind("fecha", fecha)
                .bind("idUbicacion", idUbicacion)
                .bind("limit", safeSize)
                .bind("offset", offset);

        DatabaseClient.GenericExecuteSpec countSpec = databaseClient.sql(countSql)
                .bind("fecha", fecha)
                .bind("idUbicacion", idUbicacion);

        if (hasSearch) {
            selectSpec = selectSpec.bind("search", search.trim());
            countSpec  = countSpec.bind("search", search.trim());
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

    private PartidaProduccion mapRow(Row row) {
        return PartidaProduccion.builder()
                .id(row.get("id", Integer.class))
                .status(row.get("status", Integer.class))
                .nivelCritico(row.get("nivel_critico", Integer.class))
                .descripcionMotivo(row.get("descripcion", String.class))
                .isObservado(row.get("is_observado", Integer.class))
                .rollosDeclarados(row.get("rollos_declarados", Integer.class))
                .cntRollos(row.get("cnt_rollo", Integer.class))
                .fecProgramacion(row.get("fec_programacion", LocalDate.class))
                .idTipoMaquina(row.get("id_tipo_maquina", Integer.class))
                .idMaquina(row.get("id_maquina", Integer.class))
                .idPartida(row.get("id_partida", Integer.class))
                .codPartida(row.get("cod_partida", String.class))
                .idCliente(row.get("id_cliente", Integer.class))
                .razonSocial(row.get("razon_social", String.class))
                .descArticulo(row.get("desc_articulo", String.class))
                .codReceta(row.get("cod_receta", String.class))
                .descReceta(row.get("desc_receta", String.class))
                .descMaq(row.get("desc_maq", String.class))
                .isVale(row.get("is_vale", Long.class))
                .fecRealInicio(row.get("fec_real_inicio", LocalDate.class))
                .fecRealFin(row.get("fec_real_fin", LocalDate.class))
                .isDeclarado(row.get("is_declarado", Integer.class))
                .build();
    }
}
