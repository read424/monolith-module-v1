package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuiaRemisionDataProjection;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.GuiaRemisionItemProjection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GuiaRemisionKafkaRepository extends ReactiveCrudRepository<OrdenSalidaEntity, Long> {

        /**
         * Obtiene los datos principales de la guía de remisión
         * SQL: SELECT o.id_cliente, d.fec_entrega, d.id_motivo
         * FROM almacenes.ordensalida o
         * LEFT OUTER JOIN almacenes.devolucion_servicios d ON d.id_ordensalida =
         * o.id_ordensalida
         * WHERE o.id_ordensalida = :idOrdenSalida
         */
        @Query("SELECT o.id_cliente, d.fec_entrega, d.id_motivo " +
                        "FROM almacenes.ordensalida o " +
                        "LEFT OUTER JOIN almacenes.devolucion_servicios d ON d.id_ordensalida = o.id_ordensalida " +
                        "WHERE o.id_ordensalida = :idOrdenSalida")
        Mono<GuiaRemisionDataProjection> findGuiaRemisionData(Long idOrdenSalida);

        /**
         * Obtiene los items de la guía de remisión
         * SQL: SELECT d.id_articulo AS id_producto, d.id_ordensalida, d.cantidad,
         * d.precio
         * , d.tot_monto AS subtotal, d.id_detalle_orden
         * , d.tot_kilos AS peso
         * , COALESCE(d.id_unidad, 1 ) AS id_unidad
         * , 1 AS tipo_servicio
         * FROM almacenes.detalle_ordensalida d
         * WHERE d.id_ordensalida = :idOrdenSalida
         */
        @Query("SELECT d.id_articulo AS id_producto, d.id_ordensalida, d.cantidad, d.precio, " +
                        "d.tot_monto AS subtotal, d.id_detalle_orden, " +
                        "d.tot_kilos AS peso, " +
                        "COALESCE(d.id_unidad, 1) AS id_unidad, " +
                        "1 AS tipo_servicio " +
                        "FROM almacenes.detalle_ordensalida d " +
                        "WHERE d.id_ordensalida = :idOrdenSalida")
        Flux<GuiaRemisionItemProjection> findGuiaRemisionItems(Long idOrdenSalida);

}