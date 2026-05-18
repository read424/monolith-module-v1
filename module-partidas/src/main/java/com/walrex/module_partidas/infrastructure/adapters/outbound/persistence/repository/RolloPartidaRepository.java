package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.domain.model.RolloPartida;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class RolloPartidaRepository {

    private final DatabaseClient databaseClient;

    private static final String QUERY = """
            SELECT tdp.id_det_partida
                 , tdp.id_detordeningresopeso
                 , tdp.status
                 , d.cod_rollo
                 , d.peso_rollo
                 , a.no_almacen
                 , tdl.id_liquidacion
            FROM produccion.tb_partidas tp
            INNER JOIN produccion.tb_detail_partida tdp       ON tdp.id_partida             = tp.id_partida
            LEFT JOIN ventas.tb_det_peso_liquidaciones tdpl   ON tdpl.id_det_partida         = tdp.id_det_partida
            LEFT JOIN ventas.tb_det_liquidaciones tdl         ON tdl.id_det_liquidacion      = tdpl.id_det_liquidacion
            LEFT JOIN almacenes.detordeningresopeso d         ON d.id_detordeningresopeso    = tdp.id_detordeningresopeso
            LEFT JOIN almacenes.detordeningresopeso d2        ON (d2.id_rollo_ingreso = d.id_detordeningresopeso
                                                              OR (d2.id_rollo_ingreso IS NULL
                                                             AND  d2.id_detordeningresopeso = d.id_detordeningresopeso))
                                                             AND d2.status != 0
            LEFT JOIN almacenes.detordeningreso d3            ON d3.id_detordeningreso       = d2.id_detordeningreso
            LEFT JOIN almacenes.ordeningreso o                ON o.id_ordeningreso           = d3.id_ordeningreso
            LEFT JOIN almacenes.almacen a                     ON a.id_almacen               = o.id_almacen
            WHERE tp."condition" = 1
              AND tp.id_tipo_partida != 3
              AND tp.id_partida = :idPartida
            """;

    public Flux<RolloPartida> findRollosByPartida(Integer idPartida) {
        return databaseClient.sql(QUERY)
                .bind("idPartida", idPartida)
                .map((row, meta) -> mapRow(row))
                .all();
    }

    private RolloPartida mapRow(Row row) {
        return RolloPartida.builder()
                .idDetPartida(row.get("id_det_partida", Integer.class))
                .idDetordeningresopeso(row.get("id_detordeningresopeso", Integer.class))
                .status(row.get("status", Integer.class))
                .codRollo(row.get("cod_rollo", String.class))
                .pesoRollo(row.get("peso_rollo", Double.class))
                .noAlmacen(row.get("no_almacen", String.class))
                .idLiquidacion(row.get("id_liquidacion", Integer.class))
                .build();
    }
}
