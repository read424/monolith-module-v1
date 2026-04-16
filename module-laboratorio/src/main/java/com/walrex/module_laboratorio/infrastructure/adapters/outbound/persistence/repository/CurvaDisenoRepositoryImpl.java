package com.walrex.module_laboratorio.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
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
public class CurvaDisenoRepositoryImpl implements CurvaDisenoRepository {

    private final DatabaseClient db;

    private static final String PERSONAL_NAME = """
            NULLIF(TRIM(personal.no_apepat || ' ' || COALESCE(personal.no_apemat, '') || ', ' || personal.no_nombres), ',')
            """;

    private static final String SUPERVISOR_NAME = """
            NULLIF(TRIM(supervisor.no_apepat || ' ' || COALESCE(supervisor.no_apemat, '') || ', ' || supervisor.no_nombres), ',')
            """;

    private static final String SELECT_TEMPLATE = """
            SELECT cd.id
                 , cd.descripcion
                 , cd.curva_diseno
                 , cd.version
                 , cd.id_laboratorista
                 , {{PERSONAL_NAME}} AS laboratorista
                 , cd.id_supervisor
                 , {{SUPERVISOR_NAME}} AS supervisor
                 , cd.status
                 , cd.locked
                 , cd.created_at
                 , cd.updated_at
            FROM laboratorio.curva_diseno cd
            LEFT JOIN rrhh.tbpersonal personal ON personal.id_personal = cd.id_laboratorista
            LEFT JOIN rrhh.tbpersonal supervisor ON supervisor.id_personal = cd.id_supervisor
            """;

    private static final String WHERE_TEMPLATE = """
            WHERE (:search = ''
                OR cd.descripcion ILIKE '%' || :search || '%'
                OR cd.version ILIKE '%' || :search || '%'
                OR {{PERSONAL_NAME}} ILIKE '%' || :search || '%'
                OR {{SUPERVISOR_NAME}} ILIKE '%' || :search || '%')
            """;

    private static final String SELECT_CLAUSE = SELECT_TEMPLATE
            .replace("{{PERSONAL_NAME}}", PERSONAL_NAME)
            .replace("{{SUPERVISOR_NAME}}", SUPERVISOR_NAME);

    private static final String WHERE_CLAUSE = WHERE_TEMPLATE
            .replace("{{PERSONAL_NAME}}", PERSONAL_NAME)
            .replace("{{SUPERVISOR_NAME}}", SUPERVISOR_NAME);

    private static final String INSERT_QUERY = """
            INSERT INTO laboratorio.curva_diseno (
                descripcion,
                curva_diseno,
                version,
                id_laboratorista,
                status,
                id_supervisor,
                locked,
                created_at,
                updated_at
            )
            VALUES (
                :descripcion,
                CAST(:curvaDiseno AS jsonb),
                :version,
                :idLaboratorista,
                :status,
                :idSupervisor,
                :locked,
                :createdAt,
                :updatedAt
            )
            RETURNING id
            """;

    private static final String FIND_BY_ID_QUERY = SELECT_CLAUSE + "WHERE cd.id = :id";

    private static final String FIND_ALL_QUERY = SELECT_CLAUSE + WHERE_CLAUSE + """
            ORDER BY cd.id DESC
            LIMIT :limit OFFSET :offset
            """;

    private static final String COUNT_QUERY = """
            SELECT COUNT(*)
            FROM laboratorio.curva_diseno cd
            LEFT JOIN rrhh.tbpersonal personal ON personal.id_personal = cd.id_laboratorista
            LEFT JOIN rrhh.tbpersonal supervisor ON supervisor.id_personal = cd.id_supervisor
            """ + WHERE_CLAUSE;

    @Override
    public Mono<CurvaDiseno> save(CurvaDiseno curvaDiseno) {
        return db.sql(INSERT_QUERY)
                .bind("descripcion", curvaDiseno.getDescripcion())
                .bind("curvaDiseno", curvaDiseno.getCurvaDiseno())
                .bind("version", curvaDiseno.getVersion())
                .bind("idLaboratorista", curvaDiseno.getIdLaboratorista())
                .bind("status", curvaDiseno.getStatus())
                .bindNull("idSupervisor", Integer.class)
                .bind("locked", curvaDiseno.getLocked())
                .bind("createdAt", curvaDiseno.getCreatedAt())
                .bind("updatedAt", curvaDiseno.getUpdatedAt())
                .map(row -> row.get("id", Integer.class))
                .one()
                .flatMap(this::findById);
    }

    @Override
    public Mono<CurvaDiseno> findById(Integer id) {
        return db.sql(FIND_BY_ID_QUERY)
                .bind("id", id)
                .map(this::mapRow)
                .one();
    }

    @Override
    public Flux<CurvaDiseno> findAll(String search, int page, int size) {
        long offset = (long) (page - 1) * size;
        return db.sql(FIND_ALL_QUERY)
                .bind("search", normalizeSearch(search))
                .bind("limit", size)
                .bind("offset", offset)
                .map(this::mapRow)
                .all();
    }

    @Override
    public Mono<Long> countAll(String search) {
        return db.sql(COUNT_QUERY)
                .bind("search", normalizeSearch(search))
                .map(row -> row.get(0, Long.class))
                .one();
    }

    private CurvaDiseno mapRow(Row row, RowMetadata metadata) {
        return CurvaDiseno.builder()
                .id(row.get("id", Integer.class))
                .descripcion(row.get("descripcion", String.class))
                .curvaDiseno(extractJson(row, "curva_diseno"))
                .version(row.get("version", String.class))
                .idLaboratorista(row.get("id_laboratorista", Integer.class))
                .laboratorista(row.get("laboratorista", String.class))
                .idSupervisor(row.get("id_supervisor", Integer.class))
                .supervisor(row.get("supervisor", String.class))
                .status(row.get("status", Integer.class))
                .locked(row.get("locked", Boolean.class))
                .createdAt(row.get("created_at", java.time.OffsetDateTime.class))
                .updatedAt(row.get("updated_at", java.time.OffsetDateTime.class))
                .build();
    }

    private String normalizeSearch(String search) {
        return search == null ? "" : search.trim();
    }

    private String extractJson(Row row, String column) {
        Object value = row.get(column);
        if (value == null) return null;
        if (value instanceof Json json) return json.asString();
        return value.toString();
    }
}
