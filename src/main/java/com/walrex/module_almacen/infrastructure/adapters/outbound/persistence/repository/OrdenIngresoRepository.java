package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.DocMovimientoIngresoKardex;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.LoteMovimientoIngreso;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
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
}