package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import java.time.LocalDate;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.RolloDisponibleDevolucionProjection;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Servicio que utiliza R2dbcTemplate para consultar rollos disponibles
 * Responsabilidad única: Ejecutar queries complejos y retornar projections
 * Evita problemas con projections de Spring Data R2DBC
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RollosDisponiblesR2dbcTemplateService {

    private final DatabaseClient databaseClient;

    /**
     * Consulta rollos disponibles para devolución usando R2dbcTemplate
     * Query complejo que no funciona bien con projections de Spring Data
     * 
     * @param idCliente  ID del cliente
     * @param idArticulo ID del artículo
     * @return Flux de RolloDisponibleDevolucionProjection
     */
    public Flux<RolloDisponibleDevolucionProjection> buscarRollosDisponiblesParaDevolucion(Integer idCliente,
            Integer idArticulo) {
        log.info("🔍 Consultando rollos disponibles con R2dbcTemplate - Cliente: {}, Artículo: {}", idCliente,
                idArticulo);

        String sql = """
                SELECT
                    rollo_ing.id_ordeningreso, rollo_ing.id_detordeningreso, rollo_ing.id_detordeningresopeso,
                    ord_ing.cod_ingreso, ord_ing.fec_ingreso AS fecha_ingreso, ord_ing.nu_comprobante AS nu_comprobante,
                    rollo_ing.status AS status_ing,
                    det_ing.id_articulo,
                    rollo_ing.cod_rollo, COALESCE(rollo_ing.peso_devolucion, rollo_ing.peso_rollo) AS peso_rollo,
                    rollo_almacen.id_ordeningreso AS id_ordeningreso_almacen,
                    rollo_almacen.id_detordeningreso AS id_detordeningreso_almacen,
                    rollo_almacen.id_detordeningresopeso AS id_detordeningresopeso_almacen,
                    COALESCE(rollo_almacen.status, rollo_ing.status) AS status_almacen,
                    o2.cod_ingreso AS cod_ingreso_almacen,
                    COALESCE(o2.id_almacen, ord_ing.id_almacen) AS id_almacen,
                    a.no_almacen,
                    tp.id_partida,
                    CASE
                        WHEN tp.id_partida_parent IS NULL THEN tp.cod_partida
                        ELSE tp.cod_partida || '-' ||
                             CASE WHEN COALESCE(tp.type_reprocess, 1) = 1 THEN 'RT' ELSE 'RA' END ||
                             tp.num_reproceso::varchar
                    END AS cod_partida,
                    COALESCE(tp.add_cobro, 0) AS sin_cobro,
                    tdp.id_det_partida, tdp.status AS status_roll_partida
                FROM almacenes.detordeningreso AS det_ing
                INNER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso = det_ing.id_ordeningreso
                INNER JOIN almacenes.detordeningresopeso AS rollo_ing ON rollo_ing.id_detordeningreso = det_ing.id_detordeningreso
                    AND rollo_ing.status IN (0, 1)
                LEFT OUTER JOIN almacenes.detordeningresopeso AS rollo_almacen ON rollo_almacen.id_rollo_ingreso = rollo_ing.id_detordeningresopeso
                    AND rollo_almacen.status = 1
                LEFT OUTER JOIN almacenes.ordeningreso o2 ON o2.id_ordeningreso = rollo_almacen.id_ordeningreso
                LEFT OUTER JOIN produccion.tb_detail_partida tdp ON tdp.id_detordeningresopeso = rollo_ing.id_detordeningresopeso
                    AND tdp.reproceso = 0 AND tdp.status IN (1, 4)
                LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = tdp.id_partida
                LEFT OUTER JOIN almacenes.almacen a ON a.id_almacen = COALESCE(o2.id_almacen, ord_ing.id_almacen)
                WHERE det_ing.id_articulo = :idArticulo
                AND ord_ing.id_cliente = :idCliente
                ORDER BY ord_ing.fec_ingreso DESC, rollo_ing.cod_rollo
                """;

        return databaseClient.sql(sql)
                .bind("idCliente", idCliente)
                .bind("idArticulo", idArticulo)
                .map((row, metadata) -> mapRowToProjection(row))
                .all()
                .doOnNext(projection -> log.debug("✅ Rollo projection encontrado: {} - Almacén: {}",
                        projection.getCodRollo(), projection.getIdAlmacen()))
                .doOnError(error -> log.error("❌ Error al consultar rollos con R2dbcTemplate: {}", error.getMessage()))
                .onErrorMap(throwable -> new RuntimeException("Error al consultar rollos disponibles con R2dbcTemplate",
                        throwable));
    }

    /**
     * Mapea una fila de resultado a RolloDisponibleDevolucionProjection
     * Responsabilidad única: Transformar Row a Projection
     */
    private RolloDisponibleDevolucionProjection mapRowToProjection(Row row) {
        return RolloDisponibleDevolucionProjection.builder()
                .idOrdeningreso(row.get("id_ordeningreso", Integer.class))
                .idDetordeningreso(row.get("id_detordeningreso", Integer.class))
                .idDetordeningresopeso(row.get("id_detordeningresopeso", Integer.class))
                .codIngreso(row.get("cod_ingreso", String.class))
                .fechaIngreso(row.get("fecha_ingreso", LocalDate.class))
                .nuComprobante(row.get("nu_comprobante", String.class))
                .statusIng(row.get("status_ing", Integer.class))
                .idArticulo(row.get("id_articulo", Integer.class))
                .codRollo(row.get("cod_rollo", String.class))
                .pesoRollo(row.get("peso_rollo", Double.class))
                .idOrdeningresoAlmacen(row.get("id_ordeningreso_almacen", Integer.class))
                .idDetordeningresoAlmacen(row.get("id_detordeningreso_almacen", Integer.class))
                .idDetordeningresopesoAlmacen(row.get("id_detordeningresopeso_almacen", Integer.class))
                .statusAlmacen(row.get("status_almacen", Integer.class))
                .codIngresoAlmacen(row.get("cod_ingreso_almacen", String.class))
                .idAlmacen(row.get("id_almacen", Integer.class))
                .noAlmacen(row.get("no_almacen", String.class))
                .idPartida(row.get("id_partida", Integer.class))
                .codPartida(row.get("cod_partida", String.class))
                .sinCobro(row.get("sin_cobro", Integer.class))
                .idDetPartida(row.get("id_det_partida", Integer.class))
                .statusRollPartida(row.get("status_roll_partida", Integer.class))
                .build();
    }
}