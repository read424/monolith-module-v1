package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.DetalleIngresoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.ItemRolloProjection;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Repository para consultas de detalle de ingreso usando R2dbcTemplate
 * Ejecuta consultas SQL complejas con múltiples JOINs
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DetalleIngresoRepository {

    private final DatabaseClient databaseClient;

    /**
     * Consulta el detalle de ingreso principal
     * 
     * @param idAlmacen ID del almacén
     * @return Flux de proyecciones DetalleIngresoProjection
     */
    public Flux<DetalleIngresoProjection> findDetalleIngreso(Integer idAlmacen, Integer idPartida) {
        String sql = """
                SELECT d.id_articulo, t.cod_articulo, t.desc_articulo, d.id_detordeningreso, d.id_ordeningreso, d.lote
                , t.id_tipo_producto, t.id_unidad, tu.abrev_unidad, COUNT(DISTINCT dp.id_detordeningresopeso) AS cnt_rollos
                FROM almacenes.ordeningreso o
                INNER JOIN almacenes.detordeningreso d ON d.id_ordeningreso= o.id_ordeningreso
                INNER JOIN almacenes.detordeningresopeso dp ON dp.id_detordeningreso = d.id_detordeningreso AND dp.status!=0
                LEFT OUTER JOIN logistica.tbarticulos t ON t.id_articulo = d.id_articulo
                LEFT OUTER JOIN logistica.tbunidad tu ON tu.id_unidad = t.id_unidad
                WHERE o.status=1 AND o.id_almacen = :idAlmacen AND d.id_comprobante = :idPartida
                GROUP BY o.id_ordeningreso, d.id_detordeningreso, t.id_articulo, tu.id_unidad
                ORDER BY o.id_ordeningreso ASC
                """;

        log.debug("Ejecutando consulta de detalle de ingreso para almacén ID: {}", idAlmacen);

        return databaseClient.sql(sql)
                .bind("idAlmacen", idAlmacen)
                .bind("idPartida", idPartida)
                .map(this::mapToDetalleIngresoProjection)
                .all();
    }

    /**
     * Consulta los rollos disponibles para una partida específica
     * 
     * @param idPartida ID de la partida
     * @param idAlmacen ID del almacén
     * @return Flux de proyecciones ItemRolloProjection
     */
    public Flux<ItemRolloProjection> findRollosByPartida(Integer idPartida, Integer idAlmacen) {
        String sql = """
                SELECT det_peso_ing.cod_rollo, CASE WHEN tdpl.id_det_peso_liquidacion IS NULL THEN false ELSE true END AS despachado
                , a.id_almacen, tdp.id_det_partida, ing_alm.id_ordeningreso AS id_ingreso_almacen
                , tdp.id_detordeningresopeso AS id_ingresopeso, ord_ing.id_ordeningreso
                , det_peso_alm.id_detordeningresopeso AS id_rollo_ingreso
                , tdpl.is_parent_rollo, a.no_almacen, tdpl.num_child_roll, tdp.peso_acabado
                , det_peso_ing.peso_rollo, tdp.peso_saldo, COALESCE(tdpl.peso_salida, tdp.peso_acabado) AS peso_salida
                , det_peso_alm.status
                FROM produccion.tb_detail_partida tdp
                LEFT OUTER JOIN ventas.tb_det_peso_liquidaciones tdpl ON tdpl.id_det_partida = tdp.id_det_partida
                LEFT OUTER JOIN almacenes.detordeningresopeso AS det_peso_ing ON det_peso_ing.id_detordeningresopeso = tdp.id_detordeningresopeso
                LEFT OUTER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso = det_peso_ing.id_ordeningreso
                LEFT OUTER JOIN almacenes.detordeningresopeso AS det_peso_alm ON det_peso_alm.id_rollo_ingreso = det_peso_ing.id_detordeningresopeso AND det_peso_alm.status!=0
                INNER JOIN almacenes.detordeningreso det_ing ON det_ing.id_detordeningreso = COALESCE(det_peso_alm.id_detordeningreso, det_peso_ing.id_detordeningreso) AND det_ing.id_comprobante = tdp.id_partida
                LEFT OUTER JOIN almacenes.ordeningreso ing_alm ON ing_alm.id_ordeningreso = det_ing.id_ordeningreso
                LEFT OUTER JOIN almacenes.almacen a ON a.id_almacen = COALESCE(ing_alm.id_almacen, ord_ing.id_almacen )
                WHERE tdp.reproceso = 0 AND tdp.id_partida = :idPartida AND ing_alm.id_almacen = :idAlmacen
                """;

        log.debug("Ejecutando consulta de rollos para partida ID: {} en almacén ID: {}", idPartida, idAlmacen);

        return databaseClient.sql(sql)
                .bind("idPartida", idPartida)
                .bind("idAlmacen", idAlmacen)
                .map(this::mapToItemRolloProjection)
                .all();
    }

    /**
     * Mapea una fila de resultado a DetalleIngresoProjection
     */
    private DetalleIngresoProjection mapToDetalleIngresoProjection(Row row, RowMetadata metadata) {
        return DetalleIngresoProjection.builder()
                .idArticulo(row.get("id_articulo", Integer.class))
                .codArticulo(row.get("cod_articulo", String.class))
                .descArticulo(row.get("desc_articulo", String.class))
                .idDetordeningreso(row.get("id_detordeningreso", Integer.class))
                .idOrdeningreso(row.get("id_ordeningreso", Integer.class))
                .lote(row.get("lote", String.class))
                .idTipoProducto(row.get("id_tipo_producto", Integer.class))
                .idUnidad(row.get("id_unidad", Integer.class))
                .abrevUnidad(row.get("abrev_unidad", String.class))
                .cntRollos(row.get("cnt_rollos", Integer.class))
                .build();
    }

    /**
     * Mapea una fila de resultado a ItemRolloProjection
     */
    private ItemRolloProjection mapToItemRolloProjection(Row row, RowMetadata metadata) {
        return ItemRolloProjection.builder()
                .codRollo(row.get("cod_rollo", String.class))
                .despacho(row.get("despachado", Boolean.class))
                .disabled(null) // Campo no presente en el SQL
                .idAlmacen(row.get("id_almacen", Integer.class))
                .idDetPartida(row.get("id_det_partida", Integer.class))
                .idIngresoAlmacen(row.get("id_ingreso_almacen", Integer.class))
                .idIngresopeso(row.get("id_ingresopeso", Integer.class))
                .idOrdeningreso(row.get("id_ordeningreso", Integer.class))
                .idRolloIngreso(row.get("id_rollo_ingreso", Integer.class))
                .isParentRollo(row.get("is_parent_rollo", Integer.class))
                .noAlmacen(row.get("no_almacen", String.class))
                .numChildRoll(row.get("num_child_roll", Integer.class))
                .pesoAcabado(row.get("peso_acabado", Double.class))
                .pesoRollo(row.get("peso_rollo", Double.class))
                .pesoSaldo(row.get("peso_saldo", Double.class))
                .pesoSalida(row.get("peso_salida", Double.class))
                .status(row.get("status", Integer.class))
                .build();
    }
}
