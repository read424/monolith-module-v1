package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.UnliftedRollProjection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DisableRollsQueryRepository {

    private final DatabaseClient databaseClient;

    private static final int STATUS_DISABLED = 10;

    /**
     * Obtiene los rollos sin levantamiento asignado para un periodo
     */
    public Flux<UnliftedRollProjection> findUnliftedRollsByPeriodo(Integer idPeriodo) {
        log.debug("[Repository] findUnliftedRollsByPeriodo - idPeriodo: {}", idPeriodo);

        String sql = """
            SELECT drr.id_detordeningresopeso, drr.id_levantamiento, drr.id_detordeningresopeso_alm,
                   dp.status AS status_crudo,
                   dp2.id_detordeningresopeso AS id_detordeningresopeso_almacen,
                   dp2.status AS status_almacen
            FROM revision_crudo.ingreso_revision ir
            INNER JOIN revision_crudo.detail_ingreso_revision dir ON dir.id_revision = ir.id_revision
            INNER JOIN revision_crudo.detail_rollo_revision drr ON drr.id_detail = dir.id_detail AND drr.id_levantamiento IS NULL
            LEFT OUTER JOIN revision_crudo.levantamiento l ON l.id = drr.id_levantamiento
            LEFT OUTER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningresopeso = drr.id_detordeningresopeso
            LEFT OUTER JOIN almacenes.detordeningresopeso dp2 ON dp2.id_rollo_ingreso = dp.id_detordeningresopeso
                AND dp2.status NOT IN (0, 3, 8, 5, 10, 9)
            WHERE ir.id_periodo = $1 AND dp.status NOT IN (10, 3, 8, 5, 10, 9)
            """;

        return databaseClient.sql(sql)
            .bind(0, idPeriodo)
            .map(this::mapToUnliftedRoll)
            .all()
            .doOnSubscribe(s -> log.debug("[Repository] findUnliftedRollsByPeriodo - ejecutando consulta"))
            .doOnNext(roll -> log.trace("[Repository] findUnliftedRollsByPeriodo - rollo: idPeso={}, idAlm={}, statusAlm={}",
                roll.getIdDetOrdenIngresoPeso(), roll.getIdDetOrdenIngresoPesoAlm(), roll.getStatusAlmacen()))
            .doOnComplete(() -> log.debug("[Repository] findUnliftedRollsByPeriodo - consulta completada"))
            .doOnError(error -> log.error("[Repository] findUnliftedRollsByPeriodo - ERROR: {} - Clase: {}",
                error.getMessage(), error.getClass().getSimpleName(), error));
    }

    /**
     * Deshabilita un rollo actualizando su status a 10
     */
    public Mono<Integer> updateRollStatusToDisabled(Integer idDetOrdenIngresoPeso) {
        log.debug("[Repository] updateRollStatusToDisabled - idDetOrdenIngresoPeso: {}", idDetOrdenIngresoPeso);

        if (idDetOrdenIngresoPeso == null) {
            log.warn("[Repository] updateRollStatusToDisabled - idDetOrdenIngresoPeso es null, omitiendo");
            return Mono.just(0);
        }

        String sql = """
            UPDATE almacenes.detordeningresopeso
            SET status = $1
            WHERE id_detordeningresopeso = $2
            """;

        return databaseClient.sql(sql)
            .bind(0, STATUS_DISABLED)
            .bind(1, idDetOrdenIngresoPeso)
            .fetch()
            .rowsUpdated()
            .map(Long::intValue)
            .doOnSubscribe(s -> log.debug("[Repository] updateRollStatusToDisabled - ejecutando UPDATE"))
            .doOnSuccess(count -> log.debug("[Repository] updateRollStatusToDisabled - actualizado: {} filas para id={}",
                count, idDetOrdenIngresoPeso))
            .doOnError(error -> log.error("[Repository] updateRollStatusToDisabled - ERROR para id={}: {} - Clase: {}",
                idDetOrdenIngresoPeso, error.getMessage(), error.getClass().getSimpleName(), error));
    }

    private UnliftedRollProjection mapToUnliftedRoll(Row row, RowMetadata metadata) {
        return UnliftedRollProjection.builder()
            .idDetOrdenIngresoPeso(row.get("id_detordeningresopeso", Integer.class))
            .idLevantamiento(row.get("id_levantamiento", Integer.class))
            .idDetOrdenIngresoPesoAlm(row.get("id_detordeningresopeso_alm", Integer.class))
            .statusCrudo(row.get("status_crudo", Integer.class))
            .idDetOrdenIngresoPesoAlmacen(row.get("id_detordeningresopeso_almacen", Integer.class))
            .statusAlmacen(row.get("status_almacen", Integer.class))
            .build();
    }
}
