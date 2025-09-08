package com.walrex.module_partidas.application.ports.output;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para operaciones de persistencia de órdenes de salida
 * Define el contrato para crear órdenes de salida en el almacén
 */
public interface OrdenSalidaPersistencePort {

    /**
     * Crea una nueva orden de salida
     * 
     * @param idAlmacenOrigen ID del almacén origen
     * @param idAlmacenDestino ID del almacén destino
     * @param fecRegistro Fecha de registro
     * @param idUsuario ID del usuario
     * @param idDocumentoRef ID del documento de referencia
     * @return Mono con el ID de la orden de salida creada
     */
    Mono<Integer> crearOrdenSalida(
            Integer idAlmacenOrigen,
            Integer idAlmacenDestino,
            OffsetDateTime fecRegistro,
            Integer idUsuario,
            Integer idDocumentoRef
    );

    /**
     * Crea un detalle de orden de salida
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
    Mono<Integer> crearDetalleOrdenSalida(
            Integer idOrdenSalida,
            Integer idArticulo,
            Integer idUnidad,
            Integer cantidad,
            Integer idPartida,
            BigDecimal totKilos,
            Integer idDetOrdenIngreso
    );

    /**
     * Crea un detalle de peso de orden de salida
     * 
     * @param idDetalleOrden ID del detalle de orden
     * @param idOrdenSalida ID de la orden de salida
     * @param codRollo Código del rollo
     * @param pesoRollo Peso del rollo
     * @param idDetPartida ID del detalle de partida
     * @param idRolloIngreso ID del rollo de ingreso
     * @return Mono vacío (operación completada)
     */
    Mono<Void> crearDetOrdenSalidaPeso(
            Integer idDetalleOrden,
            Integer idOrdenSalida,
            String codRollo,
            BigDecimal pesoRollo,
            Integer idDetPartida,
            Integer idRolloIngreso
    );
}
