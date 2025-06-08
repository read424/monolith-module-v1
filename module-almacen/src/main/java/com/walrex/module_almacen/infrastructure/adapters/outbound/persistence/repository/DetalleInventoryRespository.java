package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleInventaryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DetalleInventoryRespository extends ReactiveCrudRepository<DetalleInventaryEntity, Long> {

    @Query("SELECT * FROM almacenes.detalle_inventario WHERE id_lote=:idLote")
    Mono<DetalleInventaryEntity> getStockLote(Integer idLote);

    @Query("SELECT * FROM almacenes.detalle_inventario WHERE id_detordeningreso=:idDetalleOrdenIngreso")
    Mono<DetalleInventaryEntity> getInventarioByDetailIngreso(Integer idDetalleOrdenIngreso);
}
