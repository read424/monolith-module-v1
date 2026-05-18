package com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.user.module_users.domain.model.PagedResponse;
import com.walrex.user.module_users.domain.model.PersonalItem;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalRepository {

    private final DatabaseClient databaseClient;

    private static final String BASE_FROM = """
            FROM rrhh.tbpersonal tp
            INNER JOIN rrhh.tcdet_personal tcd ON tcd.id_personal    = tp.id_personal
                                              AND tcd.id_det_personal = tp.id_det_personal
            LEFT JOIN rrhh.tbarea ta           ON ta.id_area          = tcd.id_area
            WHERE tcd.id_status != 13
            """;

    private static final String SEARCH_FILTER = """
              AND TRIM(UPPER(tp.no_apepat || ' ' || tp.no_apemat)) || ', ' || tp.no_nombres
                  ILIKE '%' || :search || '%'
            """;

    private static final String AREA_FILTER = "  AND tcd.id_area = ANY(:idAreas)\n";

    private static final String SELECT_COLS = """
            SELECT tp.id_personal
                 , tp.no_apepat
                 , tp.no_apemat
                 , tp.no_nombres
                 , tp.nu_doc
                 , tcd.id_det_personal
                 , tcd.id_area
                 , ta.no_area
                 , tcd.id_cargo
                 , tcd.id_status
            """;

    private static final String ORDER_LIMIT =
            "ORDER BY tp.no_apepat, tp.no_apemat, tp.no_nombres\nLIMIT :limit OFFSET :offset\n";

    private static final String COUNT_WRAP =
            "SELECT COUNT(*) FROM rrhh.tbpersonal tp\n" +
            "INNER JOIN rrhh.tcdet_personal tcd ON tcd.id_personal = tp.id_personal AND tcd.id_det_personal = tp.id_det_personal\n" +
            "WHERE tcd.id_status != 13\n";

    public Mono<PagedResponse<PersonalItem>> listPersonal(String search, List<Integer> idAreas, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int offset = safePage * safeSize;

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasAreas = idAreas != null && !idAreas.isEmpty();

        String filters = (hasSearch ? SEARCH_FILTER : "") + (hasAreas ? AREA_FILTER : "");
        String selectSql = SELECT_COLS + BASE_FROM + filters + ORDER_LIMIT;
        String countSql = COUNT_WRAP + filters;

        DatabaseClient.GenericExecuteSpec selectSpec = databaseClient.sql(selectSql)
                .bind("limit", safeSize)
                .bind("offset", offset);
        DatabaseClient.GenericExecuteSpec countSpec = databaseClient.sql(countSql);

        if (hasSearch) {
            selectSpec = selectSpec.bind("search", search.trim());
            countSpec = countSpec.bind("search", search.trim());
        }
        if (hasAreas) {
            Integer[] areasArray = idAreas.toArray(new Integer[0]);
            selectSpec = selectSpec.bind("idAreas", areasArray);
            countSpec = countSpec.bind("idAreas", areasArray);
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

    private PersonalItem mapRow(Row row) {
        return PersonalItem.builder()
                .idPersonal(row.get("id_personal", Integer.class))
                .noApepat(row.get("no_apepat", String.class))
                .noApemat(row.get("no_apemat", String.class))
                .noNombres(row.get("no_nombres", String.class))
                .nuDoc(row.get("nu_doc", String.class))
                .idDetPersonal(row.get("id_det_personal", Integer.class))
                .idArea(row.get("id_area", Integer.class))
                .noArea(row.get("no_area", String.class))
                .idCargo(row.get("id_cargo", Integer.class))
                .idStatus(row.get("id_status", Integer.class))
                .build();
    }
}
