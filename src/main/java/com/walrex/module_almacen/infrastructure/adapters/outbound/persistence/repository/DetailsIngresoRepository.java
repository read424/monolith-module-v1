package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import org.springframework.data.r2dbc.repository.Query;
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
}
