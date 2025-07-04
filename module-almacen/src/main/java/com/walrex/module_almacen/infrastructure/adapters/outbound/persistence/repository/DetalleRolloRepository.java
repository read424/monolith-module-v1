package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.RolloDisponibleDevolucionProjection;

import reactor.core.publisher.Flux;

@Repository
public interface DetalleRolloRepository extends ReactiveCrudRepository<DetalleRolloEntity, Integer> {

    /**
     * Consulta rollos disponibles para devolución por cliente y artículo
     * Excluye rollos ya devueltos y solo incluye activos
     */
    @Query("""
            SELECT
                rollo_ing.id_detordeningresopeso,
                rollo_ing.id_detordeningreso,
                rollo_ing.id_ordeningreso,
                ord_ing.cod_ingreso,
                ord_ing.fec_ingreso AS fecha_ingreso,
                ord_ing.nu_comprobante,
                rollo_ing.status AS status_ing,
                det_ing.id_articulo,
                rollo_ing.cod_rollo,
                rollo_almacen.status AS status_almacen,
                tdp.id_det_partida,
                tp.id_partida,
                CASE
                    WHEN tp.id_partida_parent IS NULL THEN tp.cod_partida
                    ELSE tp.cod_partida || '-' ||
                         CASE WHEN COALESCE(tp.type_reprocess, 1) = 1 THEN 'RT' ELSE 'RA' END ||
                         tp.num_reproceso::varchar
                END AS cod_partida,
                COALESCE(tp.sin_cobro, '0') AS sin_cobro,
                tdp.status AS status_roll_partida,
                o2.cod_ingreso AS cod_ingreso_almacen,
                o2.id_almacen,
                a.no_almacen
            FROM almacenes.detordeningreso AS det_ing
            INNER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso = det_ing.id_ordeningreso
            INNER JOIN almacenes.detordeningresopeso AS rollo_ing ON rollo_ing.id_detordeningreso = det_ing.id_detordeningreso
                AND rollo_ing.status IN (0, 1)
            INNER JOIN almacenes.detordeningresopeso AS rollo_almacen ON rollo_almacen.id_rollo_ingreso = rollo_ing.id_detordeningresopeso
                AND rollo_almacen.status = 1
            LEFT OUTER JOIN almacenes.ordeningreso o2 ON o2.id_ordeningreso = rollo_almacen.id_ordeningreso
            LEFT OUTER JOIN produccion.tb_detail_partida tdp ON tdp.id_detordeningresopeso = rollo_ing.id_detordeningresopeso
                AND tdp.reproceso = 0 AND tdp.status IN (1, 4)
            LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = tdp.id_partida
            LEFT OUTER JOIN almacenes.almacen a ON a.id_almacen = COALESCE(o2.id_almacen, ord_ing.id_almacen)
            WHERE det_ing.id_articulo = :idArticulo
            AND ord_ing.id_cliente = :idCliente
            ORDER BY ord_ing.fec_ingreso DESC, rollo_ing.cod_rollo
            """)
    Flux<RolloDisponibleDevolucionProjection> buscarRollosDisponiblesParaDevolucion(
            Integer idCliente,
            Integer idArticulo);
}
