package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DetailsIngresoRepository extends ReactiveCrudRepository<DetailsIngresoEntity, Long> {

    @Query("INSERT INTO almacenes.detordeningreso (id_ordeningreso, id_articulo, id_unidad, nu_rollos, costo_compra) "+
            "VALUES(:id_ordeningreso, :id_articulo, :id_unidad, :nu_rollos, :costo_compra)")
    Mono<DetailsIngresoEntity> addDetalleIngreso(Integer id_ordeningreso, Integer id_articulo, Integer id_unidad, Double nu_rollos, Double costo_compra);

    @Query("DELETE FROM almacenes.detordeningreso WHERE id_ordeningreso=:idOrdeningreso")
    Mono<Integer> deleteDetailsIngreso(Integer idOrdeningreso);

    @Modifying
    @Query("""
            UPDATE almacenes.detordeningreso
            SET peso_alm = COALESCE(peso_alm, 0) + :peso,
                peso_dif = peso_ref - (COALESCE(peso_alm, 0) + :peso)
            WHERE id_detordeningreso = :idDetOrdenIngreso
            """)
    Mono<Integer> incrementPesoAlmacen(@Param("idDetOrdenIngreso") Integer idDetOrdenIngreso, @Param("peso") Double peso);

    @Query("SELECT id_ordeningreso FROM almacenes.detordeningreso WHERE id_detordeningreso = :idDetalleOrden")
    Mono<Long> findIdOrdenIngresoByIdDetalleOrden(@Param("idDetalleOrden") Integer idDetalleOrden);

    @Query("SELECT COUNT(*) FROM almacenes.detordeningresopeso WHERE id_detordeningreso = :idDetalleOrden")
    Mono<Long> countExistingRolls(@Param("idDetalleOrden") Integer idDetalleOrden);

    @Modifying
    @Query("""
            UPDATE almacenes.detordeningreso
            SET id_articulo = :idArticulo,
                peso_ref = :pesoRef,
                nu_rollos = :nuRollos,
                peso_dif = :pesoRef - peso_alm
            WHERE id_detordeningreso = :idDetalleOrden
            """)
    Mono<Integer> updateGuideArticle(
            @Param("idDetalleOrden") Integer idDetalleOrden,
            @Param("idArticulo") Integer idArticulo,
            @Param("pesoRef") Double pesoRef,
            @Param("nuRollos") Integer nuRollos);
}
