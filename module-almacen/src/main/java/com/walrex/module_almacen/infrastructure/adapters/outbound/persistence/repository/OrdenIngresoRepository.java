package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.DocMovimientoIngresoKardex;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuidePendingProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.LoteMovimientoIngreso;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface OrdenIngresoRepository extends ReactiveCrudRepository<OrdenIngresoEntity, Long> {

    @Query("INSERT INTO almacenes.ordeningreso (id_motivo, observacion, fec_ingreso, fec_ref, id_almacen) "+
            "VALUES(:id_motivo, :observacion, :fec_ingreso, :fec_ingreso, :id_almacen)")
    Mono<OrdenIngresoEntity> agregarIngreso(Integer id_motivo, String observacion, LocalDate fec_ingreso, Integer id_almacen);

    @Query("DELETE FROM almacenes.ordeningreso WHERE id_ordeningreso=:idOrdeningreso")
    Mono<Integer> deleteOrdeingresoByIdOrdeningreso(Integer idOrdeingreso);

    @Query("SELECT ord_ing.id_ordeningreso, ord_ing.cod_ingreso, ord_ing.fec_ingreso, mot.no_motivo "+
            "FROM almacenes.ordeningreso AS ord_ing "+
            "LEFT OUTER JOIN almacenes.tbmotivos AS mot ON mot.id_motivo=ord_ing.id_motivo "+
            "WHERE ord_ing.status=1 AND ord_ing.id_ordeningreso=:idOrdeningreso")
    Mono<DocMovimientoIngresoKardex> detalleDocumentoIngreso(Integer idOrdeningreso);

    @Query("SELECT det_inv.id_lote, ord_ing.id_ordeningreso, det_inv.id_detordeningreso, ord_ing.cod_ingreso, mot.no_motivo "+
            "FROM almacenes.detalle_inventario AS det_inv "+
            "LEFT OUTER JOIN almacenes.detordeningreso AS det_ing ON det_ing.id_detordeningreso=det_inv.id_detordeningreso "+
            "LEFT OUTER JOIN almacenes.ordeningreso AS ord_ing ON ord_ing.id_ordeningreso=det_ing.id_ordeningreso "+
            "LEFT OUTER JOIN almacenes.tbmotivos AS mot ON mot.id_motivo=ord_ing.id_motivo "+
            "WHERE det_inv.id_loge=:idLote")
    Mono<LoteMovimientoIngreso> getInfoIngresoByIdLote(Integer idLote);

    @Query("""
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
        """)
    Flux<GuidePendingProjection> findPendingGuides(LocalDate date);
}