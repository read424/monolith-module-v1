package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.PartidaInfoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.RollsInStoreProjection;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidaDetailsRepository {

    private final DatabaseClient databaseClient;


    public Mono<PartidaInfoProjection> getInfoPartidaById(Integer idPartida){
        String sql = """
                SELECT part.id_partida, part.cod_partida, part.cnt_rollo, part.total_kg, ord_prod.id_ruta
                , part.id_receta, part.status, part.id_cliente, part.id_ordenproduccion, part.id_tipo_acabado
                , part.id_receta_acabado, part.color_referencia, part.id_articulo, part.id_partida_parent
                , part.num_reproceso, part.condition, part.fec_programado, part.add_cobro, part.type_reprocess
                , part.peso_preparado, det_ing.lote
                FROM produccion.tb_partidas AS part
                LEFT OUTER JOIN comercial.tborden_produccion AS ord_prod ON ord_prod.id_ordenproduccion = part.id_ordenproduccion
                LEFT OUTER JOIN almacenes.detordeningreso AS det_ing ON det_ing.id_ordeningreso=part.id_ordeningreso AND det_ing.id_articulo=ord_prod.id_articulo
                WHERE part.id_partida=:id_partida AND part.condition=1 AND part.status IN (1, 0)
            """;

        return databaseClient.sql(sql)
            .bind("id_partida", idPartida)
            .map( (row, metadata) -> mapToProjectionPartida(row))
            .one();
    }

    public Mono<Integer> getCantidadRollsEnabled(Integer idPartida){
        String sql = """
                SELECT COUNT(det_part.id_det_partida) AS cnt_rollos
                FROM produccion.tb_partidas AS part
                LEFT OUTER JOIN produccion.tb_detail_partida AS det_part ON det_part.id_partida=part.id_partida
                WHERE part.id_partida = :idPartida AND (det_part.reproceso=0 AND det_part.status IN (1, 2))
                GROUP BY part.id_partida
            """;

        return databaseClient.sql(sql)
            .bind("idPartida", idPartida)
            .map(row -> row.get("cnt_rollos", Integer.class))
            .one()
            .doOnSuccess(id -> log.info("Cantidad Partida con ID: {}", id))
            .doOnError(error->log.error("Error consultando cantidad rollos partidas: {}", error.getMessage()));
    }

    public Flux<RollsInStoreProjection> getRollsInStored(Integer idPartida){
        String sql = """
                SELECT det_ing_peso.id_detordeningresopeso, det_ing_peso.id_ordeningreso, det_ing_peso.id_detordeningreso
                , det_ing_peso.id_rollo_ingreso, det_ing_peso.cod_rollo, det_ing_peso.peso_rollo
                FROM almacenes.detordeningreso AS det_ing
                INNER JOIN almacenes.detordeningresopeso AS det_ing_pes ON det_ing_pes.id_detordeningreso = det_ing.id_detordeningreso AND det_ing_pes.status = 1
                LEFT OUTER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso = det_ing.id_ordeningreso
                WHERE det_ing.id_comprobante = :idPartida AND ord_ing.id_almacen NOT IN (6, 7, 8, 9, 10, 11)
            """;

        return databaseClient.sql(sql)
            .bind("idPartida", idPartida)
            .map((row, metadata) -> mapToProjection(row))
            .all()
            .doOnComplete(()-> log.info("Consulta de rollos habilitados en almacenes"));
    }

    private RollsInStoreProjection mapToProjection(Row row){
        try {
            return RollsInStoreProjection.builder()
                .idDetOrdenIngresoPeso(row.get("id_detordeningresopeso", Integer.class))
                .idOrdenIngreso(row.get("id_ordeningreso", Integer.class))
                .idDetOrdenIngreso(row.get("id_detordeningreso", Integer.class))
                .idRolloIngreso(row.get("id_rollo_ingreso", Integer.class))
                .codigo(row.get("cod_rollo", String.class))
                .peso(row.get("peso_rollo", Double.class))
                .build();
        }catch(Exception e){
            throw new RuntimeException("Error en mapeo de proyection (RollsInStoreProjection)", e);
        }
    }

    private PartidaInfoProjection mapToProjectionPartida(Row row){
        try{
            return PartidaInfoProjection.builder()
                .id_partida(row.get("id_partida", Integer.class))
                .cod_partida(row.get("cod_partida", String.class))
                .cnt_rollo(row.get("cnt_rollo", Integer.class))
                .total_kg(row.get("total_kg", Double.class))
                .id_ruta(row.get("id_ruta", Integer.class))
                .id_receta(row.get("id_receta", Integer.class))
                .status(row.get("status", Integer.class))
                .id_cliente(row.get("id_cliente", Integer.class))
                .id_ordenproduccion(row.get("id_ordenproduccion", Integer.class))
                .id_tipo_acabado(row.get("id_tipo_acabado", Integer.class))
                .id_receta_acabado(row.get("id_receta_acabado", Integer.class))
                .color_referencia(row.get("color_referencia", String.class))
                .id_articulo(row.get("id_articulo", Integer.class))
                .id_partida_parent(row.get("id_partida_parent", Integer.class))
                .num_reprocesos(row.get("num_reprocesos", Integer.class))
                .condition(row.get("condition", Integer.class))
                .fec_programado(row.get("fec_programado", LocalDate.class))
                .add_cobro(row.get("add_cobro", Integer.class))
                .type_reprocess(row.get("type_reprocess", Integer.class))
                .lote(row.get("lote", String.class))
                .build();
        }catch(Exception e){
            throw new RuntimeException("Error en mapeo de proyection (PartidaInfoProjection)", e);
        }
    }

}
