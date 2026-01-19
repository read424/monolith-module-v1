package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RevisionInventarioProjection;

import reactor.core.publisher.Flux;

/**
 * Repository para ingreso_revision con query compleja
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Repository
public interface RevisionInventarioRepository extends ReactiveCrudRepository<IngresoRevisionEntity, Integer> {

    @Query("""
        SELECT o.id_ordeningreso, o.id_cliente, o.fec_ingreso, o.nu_serie, o.nu_comprobante
        , d.id_detordeningreso, d.id_articulo
        , d2.id_detordeningresopeso, tdp.id_partida, COALESCE(tdp.status, COALESCE(d3.status, d2.status)) AS status
        , CASE WHEN (tdp.id_partida IS NOT NULL AND (d2.status = 1 OR COALESCE(a.id_almacen, o.id_almacen) IN (2, 32) ) )  THEN 1 ELSE 0 END AS as_crudo
        , rollo_almac.id_detordeningresopeso AS id_detordeningresopeso_alm
        , rollo_almac.status AS status_almacen
        , COALESCE(ing_alm.id_almacen, o.id_almacen) AS id_almacen
        FROM almacenes.ordeningreso o
        INNER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
        INNER JOIN almacenes.detordeningresopeso d2 ON d2.id_detordeningreso = d.id_detordeningreso AND d2.status IN (0, 1, 2, 4, 6, 7, 10)
        LEFT OUTER JOIN produccion.tb_detail_partida tdp ON tdp.id_detordeningresopeso = d2.id_detordeningresopeso AND tdp.reproceso = 0
        LEFT OUTER JOIN almacenes.detordeningresopeso d3 ON d3.id_detordeningresopeso = tdp.id_detordeningresopeso
        LEFT OUTER JOIN almacenes.detordeningresopeso rollo_almac ON rollo_almac.id_rollo_ingreso = d2.id_detordeningresopeso AND rollo_almac.status IN (1, 2, 10)
        LEFT OUTER JOIN almacenes.ordeningreso AS ing_alm ON ing_alm.id_ordeningreso = rollo_almac.id_ordeningreso
        LEFT OUTER JOIN almacenes.almacen a ON a.id_almacen = COALESCE(ing_alm.id_almacen, o.id_almacen)
        WHERE o.id_ordeningreso IN (
            SELECT ics.id_ordeningreso
            FROM revision_crudo.ingreso_corregir_status AS ics
            WHERE ics.status_nuevo IN (10, 1)
        )
        ORDER BY o.id_ordeningreso, d.id_detordeningreso, d2.id_detordeningresopeso
        """)
    Flux<RevisionInventarioProjection> findDatosRevisionInventario();
}
