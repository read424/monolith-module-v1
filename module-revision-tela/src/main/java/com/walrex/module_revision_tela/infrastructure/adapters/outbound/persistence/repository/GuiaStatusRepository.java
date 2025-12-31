package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoCorregirStatusEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.GuiaStatusProjection;

import reactor.core.publisher.Flux;

/**
 * Repository R2DBC para consultas de status de guías
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Repository
public interface GuiaStatusRepository extends R2dbcRepository<IngresoCorregirStatusEntity, Integer> {

    /**
     * Consulta SQL que obtiene las guías con sus status calculados a partir de los rollos
     * Filtra por almacén de tela cruda (id_almacen=2) y motivo ingreso (id_motivo=1)
     */
    @Query("""
        SELECT
            o.id_ordeningreso,
            o.fec_ingreso,
            o.id_cliente,
            o.id_comprobante,
            o.nu_serie,
            o.nu_comprobante,
            o.status AS status_orden,
            CASE
                WHEN d2.status IN (3, 5, 8, 9) THEN 3
                WHEN d2.status IN (0, 1, 2, 4, 6, 7) THEN 1
                ELSE 10
            END AS status
        FROM almacenes.ordeningreso o
        INNER JOIN almacenes.detordeningreso d ON d.id_ordeningreso = o.id_ordeningreso
        INNER JOIN almacenes.detordeningresopeso d2 ON d2.id_detordeningreso = d.id_detordeningreso
        WHERE o.id_almacen = 2
            AND o.id_motivo = 1
            AND o.condicion = 1
        ORDER BY o.id_ordeningreso
        """)
    Flux<GuiaStatusProjection> findGuiasConStatusRollos();
}
