package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailSalidaLoteEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DetailSalidaLoteRepository extends ReactiveCrudRepository<DetailSalidaLoteEntity, Long> {

    @Query("INSERT INTO almacenes.detalle_salida_lote (id_detalle_orden, id_lote, cantidad, monto_consumo, total_monto, id_ordensalida) "+
            "VALUES (:id_detalle_orden, :id_lote, :cantidad, :monto_consumo, :total_monto, :id_ordensalida) ")
    Mono<DetailSalidaLoteEntity> agregarDetailSalidaLote(Integer id_detalle_orden, Integer id_lote, Double cantidad, Double monto_consumo, Double total_monto, Integer id_ordensalida);

    @Query("DELETE FROM almacenes.detalle_salida_lote WHERE id_ordensalida=:idOrdenSalida")
    Mono<Integer> deleteSalidaLotesByIdOrdenSalida(Integer idOrdenSalida);

    @Query("SELECT * FROM almacenes.detalle_salida_lote WHERE id_detalle_orden = :idDetalleOrden")
    Flux<DetailSalidaLoteEntity> findByIdDetalleOrden(Long idDetalleOrden);
}
