package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface KardexRepository extends ReactiveCrudRepository<KardexEntity, Integer> {
    /*
    Mono<KardexEntity> findTopByIdArticuloAndIdAlmacenOrderByFechaMovimientoDesc(Integer idArticulo, Integer idAlmacen);
    */
    @Query("SELECT * "+
            "FROM almacenes.kardex "+
            "WHERE id_articulo=:idArticulo AND id_almacen=:idAlmacen "+
            "ORDER BY id_kardex DESC "+
            "LIMIT 1 OFFSET 0")
    Mono<KardexEntity> findByIdArticuloAndIdAlmacen(Integer idArticulo, Integer idAlmacen);
}
