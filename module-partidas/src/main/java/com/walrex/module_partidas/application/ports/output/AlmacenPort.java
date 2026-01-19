package com.walrex.module_partidas.application.ports.output;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.PartidaInfoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.RollsInStoreProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Puerto de salida para operaciones de almacén.
 * Define el contrato para gestionar órdenes de ingreso y movimientos de rollos.
 */
public interface AlmacenPort {

    /**
     * Obtiene información completa de una partida.
     *
     * @param idPartida ID de la partida
     * @return Mono con la información de la partida
     */
    Mono<PartidaInfoProjection> obtenerInfoPartida(Integer idPartida);

    /**
     * Obtiene los rollos almacenados para una partida.
     *
     * @param idPartida ID de la partida
     * @return Flux con los rollos almacenados
     */
    Flux<RollsInStoreProjection> obtenerRollosAlmacenados(Integer idPartida);

    /**
     * Crea una orden de ingreso al almacén de calidad.
     *
     * @param idCliente ID del cliente
     * @param idOrigen  ID del almacén de origen (puede ser null)
     * @param idAlmacen ID del almacén destino
     * @return Mono con el ID de la orden creada
     */
    Mono<Integer> crearOrdenIngreso(Integer idCliente, Integer idOrigen, Integer idAlmacen);

    /**
     * Crea un detalle de orden de ingreso.
     *
     * @param idOrdenIngreso ID de la orden de ingreso
     * @param idArticulo     ID del artículo
     * @param idUnidad       ID de la unidad
     * @param pesoRef        Peso de referencia
     * @param lote           Lote del artículo
     * @param nuRollos       Número de rollos
     * @param idComprobante  ID del comprobante (idPartida)
     * @return Mono con el ID del detalle creado
     */
    Mono<Integer> crearDetalleOrdenIngreso(Integer idOrdenIngreso, Integer idArticulo, Integer idUnidad,
                                           BigDecimal pesoRef, String lote, Integer nuRollos, Integer idComprobante);

    /**
     * Crea un detalle de peso de orden de ingreso.
     *
     * @param idOrdenIngreso    ID de la orden de ingreso
     * @param codRollo          Código del rollo
     * @param pesoRollo         Peso del rollo
     * @param idDetOrdenIngreso ID del detalle de orden de ingreso
     * @param idRolloIngreso    ID del rollo de ingreso
     * @return Mono con el ID del detalle de peso creado
     */
    Mono<Integer> crearDetallePesoOrdenIngreso(Integer idOrdenIngreso, String codRollo, BigDecimal pesoRollo,
                                                Integer idDetOrdenIngreso, Integer idRolloIngreso);

    /**
     * Cambia el status de un rollo almacenado.
     *
     * @param idDetOrdenIngresoPeso ID del detalle de orden de ingreso peso
     * @param status                Nuevo status (0 = deshabilitado, 1 = habilitado)
     * @return Mono con el resultado de la operación
     */
    Mono<Boolean> cambiarStatusRollo(Integer idDetOrdenIngresoPeso, Integer status);
}
