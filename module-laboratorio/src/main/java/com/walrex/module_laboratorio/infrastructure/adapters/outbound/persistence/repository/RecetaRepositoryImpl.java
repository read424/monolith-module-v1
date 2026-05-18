package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.domain.model.CurvaDisenoItem;
import com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.projection.RecetaProjection;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RecetaRepositoryImpl implements RecetaRepository {

    private final DatabaseClient db;

    private static final String WHERE_CLAUSE = """
            WHERE tr.status = 1
              AND (
                :search = ''
                OR tr.cod_receta                       ILIKE '%' || :search || '%'
                OR UPPER(t.no_colores)                 ILIKE '%' || UPPER(:search) || '%'
                OR CASE
                     WHEN t3.id_tipodoc = 3 THEN t3.no_razon
                     ELSE TRIM(t3.no_apepat || ' ' || t3.no_apemat) || ', ' || t3.no_nombres
                   END                                 ILIKE '%' || :search || '%'
              )
            """;

    private static final String FROM_CLAUSE = """
            FROM laboratorio.tb_receta tr
            LEFT JOIN laboratorio.tbcolores t       ON t.id_colores      = tr.id_colores
            LEFT JOIN laboratorio.tbcolor t2        ON t2.id_color       = t.id_color
            LEFT JOIN comercial.tbtip_tenido tt     ON tt.id_tipo_tenido = t.id_tipo_tenido
            LEFT JOIN comercial.tbclientes t3       ON t3.id_cliente     = tr.id_cliente
            LEFT JOIN laboratorio.tbgamas gam       ON gam.id_gama       = t.id_gama
            """;

    private static final String SELECT_QUERY = """
            SELECT tr.id_receta
                 , tr.cod_receta
                 , CASE
                     WHEN t3.id_tipodoc = 3 THEN t3.no_razon
                     ELSE TRIM(t3.no_apepat || ' ' || t3.no_apemat) || ', ' || t3.no_nombres
                   END                                   AS razon_social
                 , t.cod_colores
                 , UPPER(t.no_colores)                   AS no_colores
                 , tr.status
                 , tr.compartir
                 , gam.no_gama
                 , t2.no_color
                 , COALESCE(UPPER(tt.no_tenido), 'N/E') AS no_tenido
                 , tr.curva_diseno
            """ + FROM_CLAUSE + WHERE_CLAUSE + """
            ORDER BY tr.id_receta DESC
            LIMIT :limit OFFSET :offset
            """;

    private static final String COUNT_QUERY = "SELECT COUNT(*) " + FROM_CLAUSE + WHERE_CLAUSE;

    private static final String FIND_BY_ID_QUERY = """
            SELECT tr.id_receta
                 , tr.cod_receta
                 , CASE
                     WHEN t3.id_tipodoc = 3 THEN t3.no_razon
                     ELSE TRIM(t3.no_apepat || ' ' || t3.no_apemat) || ', ' || t3.no_nombres
                   END                                   AS razon_social
                 , t.cod_colores
                 , UPPER(t.no_colores)                   AS no_colores
                 , tr.status
                 , tr.compartir
                 , gam.no_gama
                 , t2.no_color
                 , COALESCE(UPPER(tt.no_tenido), 'N/E') AS no_tenido
                 , tr.curva_diseno
            """ + FROM_CLAUSE + """
            WHERE tr.id_receta = :id
            """;

    private static final String EXISTS_BY_ID_QUERY = """
            SELECT EXISTS(
                SELECT 1
                FROM laboratorio.tb_receta
                WHERE id_receta = :id
            )
            """;

    private static final String UPDATE_CURVA_DISENO_QUERY = """
            UPDATE laboratorio.tb_receta
            SET curva_diseno = CAST(:curvaDiseno AS jsonb)
            WHERE id_receta = :id
            RETURNING id_receta
                    , cod_receta
                    , NULL::varchar AS razon_social
                    , NULL::varchar AS cod_colores
                    , NULL::varchar AS no_colores
                    , status
                    , compartir
                    , NULL::varchar AS no_gama
                    , NULL::varchar AS no_color
                    , NULL::varchar AS no_tenido
                    , curva_diseno
            """;

    @Override
    public Flux<RecetaProjection> findAllPaged(String search, long offset, int limit) {
        return db.sql(SELECT_QUERY)
                .bind("search", search)
                .bind("limit", limit)
                .bind("offset", offset)
                .map(this::mapRow)
                .all();
    }

    @Override
    public Mono<Long> countAll(String search) {
        return db.sql(COUNT_QUERY)
                .bind("search", search)
                .map(row -> row.get(0, Long.class))
                .one();
    }

    @Override
    public Mono<RecetaProjection> findById(Integer id) {
        return db.sql(FIND_BY_ID_QUERY)
                .bind("id", id)
                .map(this::mapRow)
                .one();
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return db.sql(EXISTS_BY_ID_QUERY)
                .bind("id", id)
                .map(row -> row.get(0, Boolean.class))
                .one();
    }

    private static final String GET_CURVAS_DISENO_QUERY = """
            SELECT cdr.id
                 , cdr.id_curva_diseno
                 , cd.curva_diseno AS curva
            FROM laboratorio.curva_diseno_receta cdr
            JOIN laboratorio.curva_diseno cd ON cd.id = cdr.id_curva_diseno
            WHERE cdr.id_receta = :idReceta
              AND cdr.status = 1
            ORDER BY cdr.id
            """;

    @Override
    public Mono<RecetaProjection> updateCurvaDiseno(Integer id, String curvaDiseno) {
        return db.sql(UPDATE_CURVA_DISENO_QUERY)
                .bind("id", id)
                .bind("curvaDiseno", curvaDiseno)
                .map(this::mapRow)
                .one();
    }

    @Override
    public Flux<CurvaDisenoItem> getCurvasDiseno(Integer idReceta) {
        return db.sql(GET_CURVAS_DISENO_QUERY)
                .bind("idReceta", idReceta)
                .map((row, meta) -> CurvaDisenoItem.builder()
                        .id(row.get("id", Integer.class))
                        .idCurvaDiseno(row.get("id_curva_diseno", Integer.class))
                        .curva(extractJson(row, "curva"))
                        .build())
                .all();
    }

    private RecetaProjection mapRow(Row row, RowMetadata metadata) {
        return new RecetaProjection(
                row.get("id_receta", Integer.class),
                row.get("cod_receta", String.class),
                row.get("razon_social", String.class),
                row.get("cod_colores", String.class),
                row.get("no_colores", String.class),
                row.get("status", Integer.class),
                row.get("compartir", String.class),     // CHAR(1) → String
                row.get("no_gama", String.class),
                row.get("no_color", String.class),
                row.get("no_tenido", String.class),
                extractJson(row, "curva_diseno")
        );
    }

    private String extractJson(Row row, String column) {
        Object value = row.get(column);
        if (value == null) return null;
        if (value instanceof Json json) return json.asString();
        return value.toString();
    }
}
