package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DetailSalidaRepository extends ReactiveCrudRepository<DetailSalidaEntity, Long> {

    @Query("INSERT INTO almacenes.detalle_ordensalida (id_ordensalida, id_articulo, id_unidad, cantidad, entregado, precio, tot_monto, status) "+
            " VALUES (:id_ordensalida, :id_articulo, :id_unidad, :cantidad, :entregado, :precio, :tot_monto, :status)")
    Mono<DetailSalidaEntity> agregarDetalleSalida(Integer id_ordensalida, Integer id_articulo, Integer id_unidad, Double cantidad, Integer entregado, Double precio, Double tot_monto, Integer status);

    @Query("DELETE FROM almacenes.detalle_ordensalida WHERE id_ordensalida=:idOrdenSalida")
    Mono<Integer> deleteDetailsSalidaByIdOrden(Integer idOrdenSalida);

    @Query("UPDATE almacenes.detalle_ordensalida SET entregado=1 WHERE id_detalle_orden=:idDetalleOrden")
    Mono<DetailSalidaEntity> assignedDelivered(Integer idDetalleOrden);

    @Query("SELECT * FROM almacenes.detalle_ordensalida WHERE id_ordensalida = :idOrdenSalida")
    Flux<DetailSalidaEntity> findByIdOrderSalida(Long idOrdenSalida);
}
