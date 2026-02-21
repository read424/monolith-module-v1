package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.domain.model.ArticuloPesajeSession;
import com.walrex.module_almacen.domain.model.PesajeDetalle;
import com.walrex.module_almacen.domain.model.dto.RolloPesadoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SessionPesajeCustomRepository {

    private final DatabaseClient databaseClient;

    public Mono<ArticuloPesajeSession> findArticuloWithSessionDetail(Integer idDetOrdenIngreso) {
        String query = """
                SELECT spa.id
                     , d.id_detordeningreso
                     , d.nu_rollos::integer                                          AS nu_rollos
                     , d.peso_ref
                     , COUNT(DISTINCT d2.id_detordeningresopeso)::integer            AS cnt_roll_saved
                     , COALESCE(SUM(d2.peso_rollo), 0.00)                           AS total_kg_saved
                     , COALESCE(MAX(split_part(d2.cod_rollo, '-', 3)::integer), 0)  AS last_roll_saved
                     , COALESCE(spa.status, '1')                                    AS status
                FROM almacenes.detordeningreso d
                LEFT OUTER JOIN almacenes.session_pesaje_activa spa
                       ON spa.id_detordeningreso = d.id_detordeningreso
                LEFT OUTER JOIN almacenes.detordeningresopeso d2
                       ON d2.id_detordeningreso = d.id_detordeningreso
                WHERE d.id_detordeningreso = :idDetOrdenIngreso
                GROUP BY d.id_detordeningreso, spa.id
                """;

        return databaseClient.sql(query)
                .bind("idDetOrdenIngreso", idDetOrdenIngreso)
                .map((row, metadata) -> ArticuloPesajeSession.builder()
                        .id(row.get("id", Integer.class))
                        .idDetOrdenIngreso(row.get("id_detordeningreso", Integer.class))
                        .nuRollos(row.get("nu_rollos", Integer.class) != null
                                ? row.get("nu_rollos", Integer.class) : 0)
                        .pesoRef(row.get("peso_ref", BigDecimal.class) != null
                                ? row.get("peso_ref", BigDecimal.class) : BigDecimal.ZERO)
                        .cntRollSaved(row.get("cnt_roll_saved", Integer.class) != null
                                ? row.get("cnt_roll_saved", Integer.class) : 0)
                        .totalSaved(row.get("total_kg_saved", BigDecimal.class) != null
                                ? row.get("total_kg_saved", BigDecimal.class) : BigDecimal.ZERO)
                        .lastRollSaved(row.get("last_roll_saved", Integer.class) != null
                                ? row.get("last_roll_saved", Integer.class) : 0)
                        .status(row.get("status", String.class) != null
                                ? row.get("status", String.class) : "1")
                        .build())
                .one();
    }

    public Mono<PesajeDetalle> findActiveSessionWithDetail() {
        String query = """
                SELECT spa.id
                     , spa.id_detordeningreso
                     , doi.id_ordeningreso
                     , spa.cnt_rollos
                     , spa.cnt_registro
                     , doi.lote
                FROM almacenes.session_pesaje_activa spa
                INNER JOIN almacenes.detordeningreso doi ON doi.id_detordeningreso = spa.id_detordeningreso
                WHERE spa.status = '1'
                """;

        return databaseClient.sql(query)
                .map((row, metadata) -> PesajeDetalle.builder()
                        .id_session_hidden(row.get("id", Integer.class))
                        .id_detordeningreso(row.get("id_detordeningreso", Integer.class))
                        .idOrdenIngreso(row.get("id_ordeningreso", Integer.class))
                        .cnt_rollos(row.get("cnt_rollos", Integer.class))
                        .cnt_registrados(row.get("cnt_registro", Integer.class))
                        .lote(row.get("lote", String.class))
                        .build())
                .one();
    }

    public Flux<RolloPesadoDTO> findRollosByIdDetOrdenIngreso(Integer idDetOrdenIngreso) {
        String query = """
                SELECT id_detordeningresopeso, cod_rollo, peso_rollo
                FROM almacenes.detordeningresopeso
                WHERE id_detordeningreso = :idDetOrdenIngreso
                  AND status = 1
                ORDER BY id_detordeningresopeso DESC
                """;

        return databaseClient.sql(query)
                .bind("idDetOrdenIngreso", idDetOrdenIngreso)
                .map((row, metadata) -> RolloPesadoDTO.builder()
                        .id_detordeningresopeso(row.get("id_detordeningresopeso", Integer.class))
                        .cod_rollo(row.get("cod_rollo", String.class))
                        .peso(row.get("peso_rollo", Double.class))
                        .build())
                .all();
    }

}
