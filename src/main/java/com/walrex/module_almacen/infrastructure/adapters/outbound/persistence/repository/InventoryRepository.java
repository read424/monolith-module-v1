package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.InventoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryRepository extends ReactiveCrudRepository<InventoryEntity, Long> {

    @Query("SELECT * FROM almacenes.inventario WHERE id_articulo=:idArticulo AND id_almacen=:idAlmacen")
    Mono<InventoryEntity> getStockInStorage(Integer idArticulo, Integer idAlmacen);
}
