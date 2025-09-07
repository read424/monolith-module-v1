package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrdenSalidaMovimientoRepository {

    private final DatabaseClient databaseClient;

    /**
     * Inserta una nueva orden de salida
     * 
     * @param idAlmacenOrigen ID del almacén origen
     * @param idAlmacenDestino ID del almacén destino
     * @param idMotivo ID del motivo (valor fijo: 3)
     * @param isInterno Si es interno (valor fijo: 1)
     * @param idTipoComprobante ID del tipo de comprobante (valor fijo: 3)
     * @param fecRegistro Fecha de registro
     * @param idUsuario ID del usuario
     * @param entregado Si está entregado (valor fijo: 1)
     * @param idDocumentoRef ID del documento de referencia
     * @return Mono con el ID de la orden de salida creada
     */
    public Mono<Integer> insertOrdenSalida(
            Integer idAlmacenOrigen,
            Integer idAlmacenDestino,
            OffsetDateTime fecRegistro,
            Integer idUsuario,
            Integer idDocumentoRef
    ) {
        return databaseClient
                .sql("""
                    INSERT INTO almacenes.ordensalida (id_almacen_origen, id_almacen_destino, id_motivo, is_interno, id_tipo_comprobante, 
                     fec_registro, id_usuario, entregado, id_documento_ref)
                    VALUES (:idAlmacenOrigen, :idAlmacenDestino, 3, 1, 3, :fecRegistro, :idUsuario, 1, :idDocumentoRef)
                    RETURNING id_ordensalida
                    """)
                .bind("idAlmacenOrigen", idAlmacenOrigen)
                .bind("idAlmacenDestino", idAlmacenDestino)
                .bind("fecRegistro", fecRegistro)
                .bind("idUsuario", idUsuario)
                .bind("idDocumentoRef", idDocumentoRef)
                .map((row, metadata) -> row.get("id_ordensalida", Integer.class))
                .one();
    }

    /**
     * Inserta un detalle de orden de salida
     * 
     * @param idOrdenSalida ID de la orden de salida
     * @param idArticulo ID del artículo
     * @param idUnidad ID de la unidad
     * @param cantidad Cantidad
     * @param idPartida ID de la partida
     * @param totKilos Total de kilos
     * @param idDetOrdenIngreso ID del detalle de orden de ingreso
     * @return Mono con el ID del detalle de orden creado
     */
    public Mono<Integer> insertDetalleOrdenSalida(
            Integer idOrdenSalida,
            Integer idArticulo,
            Integer idUnidad,
            Integer cantidad,
            Integer idPartida,
            BigDecimal totKilos,
            Integer idDetOrdenIngreso
    ) {
        return databaseClient
                .sql("""
                    INSERT INTO almacenes.detalle_ordensalida 
                    (id_ordensalida, id_articulo, id_unidad, cantidad, id_partida, tot_kilos, id_detordeningreso)
                    VALUES (:idOrdenSalida, :idArticulo, :idUnidad, :cantidad, :idPartida, :totKilos, :idDetOrdenIngreso)
                    RETURNING id_detalle_orden
                    """)
                .bind("idOrdenSalida", idOrdenSalida)
                .bind("idArticulo", idArticulo)
                .bind("idUnidad", idUnidad)
                .bind("cantidad", cantidad)
                .bind("idPartida", idPartida)
                .bind("totKilos", totKilos)
                .bind("idDetOrdenIngreso", idDetOrdenIngreso)
                .map((row, metadata) -> row.get("id_detalle_orden", Integer.class))
                .one();
    }

    /**
     * Inserta un detalle de peso de orden de salida
     * 
     * @param idDetalleOrden ID del detalle de orden
     * @param idOrdenSalida ID de la orden de salida
     * @param codRollo Código del rollo
     * @param pesoRollo Peso del rollo
     * @param idDetPartida ID del detalle de partida
     * @param idRolloIngreso ID del rollo de ingreso
     * @return Mono vacío (no devuelve ID)
     */
    public Mono<Void> insertDetOrdenSalidaPeso(
            Integer idDetalleOrden,
            Integer idOrdenSalida,
            String codRollo,
            BigDecimal pesoRollo,
            Integer idDetPartida,
            Integer idRolloIngreso
    ) {
        return databaseClient
                .sql("""
                    INSERT INTO almacenes.detordensalidapeso 
                    (id_detalle_orden, id_ordensalida, cod_rollo, peso_rollo, id_det_partida, id_rollo_ingreso)
                    VALUES (:idDetalleOrden, :idOrdenSalida, :codRollo, :pesoRollo, :idDetPartida, :idRolloIngreso)
                    """)
                .bind("idDetalleOrden", idDetalleOrden)
                .bind("idOrdenSalida", idOrdenSalida)
                .bind("codRollo", codRollo)
                .bind("pesoRollo", pesoRollo)
                .bind("idDetPartida", idDetPartida)
                .bind("idRolloIngreso", idRolloIngreso)
                .then();
    }
}
