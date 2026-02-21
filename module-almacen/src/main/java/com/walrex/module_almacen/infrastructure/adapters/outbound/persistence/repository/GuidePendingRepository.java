package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuidePendingProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GuidePendingRepository {

    private final DatabaseClient databaseClient;

    public Flux<GuidePendingProjection> findPendingGuides(LocalDate date) {
        String query = """
            SELECT 
                ord_ing.id_ordeningreso, 
                ord_ing.fec_registro, 
                ord_ing.nu_serie, 
                ord_ing.nu_comprobante,
                CASE 
                    WHEN client.id_tipodoc = 3 THEN client.no_razon 
                    ELSE trim(client.no_apepat || ' ' || client.no_apemat) || ', ' || trim(client.no_nombres) 
                END AS razon_social,
                det_ing.id_detordeningreso, 
                det_ing.lote, 
                artic.id_articulo, 
                artic.cod_articulo, 
                artic.desc_articulo,
                det_ing.nu_rollos AS total_rollos,
                COALESCE(MAX(split_part(det_ing_peso.cod_rollo, '-', 3)::integer), 0) AS num_rollo,
                COUNT(det_ing_peso.id_detordeningresopeso) AS rolls_saved
            FROM almacenes.ordeningreso AS ord_ing
            INNER JOIN comercial.tbclientes AS client ON client.id_cliente = ord_ing.id_cliente
            INNER JOIN almacenes.detordeningreso AS det_ing ON det_ing.id_ordeningreso = ord_ing.id_ordeningreso
            INNER JOIN logistica.tbarticulos AS artic ON artic.id_articulo = det_ing.id_articulo
            LEFT JOIN almacenes.detordeningresopeso AS det_ing_peso ON det_ing_peso.id_detordeningreso = det_ing.id_detordeningreso
            WHERE ord_ing.id_almacen = 2 
              AND ord_ing.fec_ingreso = :date
            GROUP BY 
                ord_ing.id_ordeningreso, client.id_cliente, det_ing.id_detordeningreso, artic.id_articulo
            HAVING COUNT(det_ing_peso.id_detordeningresopeso) < det_ing.nu_rollos
            ORDER BY ord_ing.fec_registro DESC
            """;

        return databaseClient.sql(query)
                .bind("date", date)
                .map((row, metadata) -> GuidePendingProjection.builder()
                        .id_ordeningreso(row.get("id_ordeningreso", Integer.class))
                        .fec_registro(row.get("fec_registro", LocalDate.class))
                        .nu_serie(row.get("nu_serie", String.class))
                        .nu_comprobante(row.get("nu_comprobante", String.class))
                        .razon_social(row.get("razon_social", String.class))
                        .id_detordeningreso(row.get("id_detordeningreso", Integer.class))
                        .lote(row.get("lote", String.class))
                        .id_articulo(row.get("id_articulo", Integer.class))
                        .cod_articulo(row.get("cod_articulo", String.class))
                        .desc_articulo(row.get("desc_articulo", String.class))
                        .total_rollos(row.get("total_rollos", Integer.class))
                        .num_rollo(row.get("num_rollo", Integer.class))
                        .rolls_saved(row.get("rolls_saved", Integer.class))
                        .build())
                .all();
    }
}
