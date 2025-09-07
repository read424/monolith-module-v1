package com.walrex.module_partidas.application.ports.output;

import java.util.List;

import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.ProcesoPartida;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.OrdenIngresoCompletaProjection;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para guardar el éxito de salida de tacho
 * Define el contrato para las operaciones de persistencia necesarias
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
public interface SaveSuccessOutTachoPort {

    /**
     * Consulta los rollos disponibles para una partida en un almacén específico
     * 
     * @param idPartida ID de la partida
     * @param idAlmacen ID del almacén
     * @return Mono con lista de rollos disponibles
     */
    Mono<List<ItemRollo>> consultarRollosDisponibles(Integer idPartida, Integer idAlmacen);

    /**
     * Consulta los procesos de una partida para encontrar el próximo almacén
     * pendiente
     * 
     * @param idPartida ID de la partida
     * @return Mono con lista de procesos de partida
     */
    Mono<List<ProcesoPartida>> consultarProcesosPartida(Integer idPartida);

    /**
     * Crea una nueva orden de ingreso
     * 
     * @param idCliente     ID del cliente
     * @param idAlmacen     ID del almacén destino
     * @param idComprobante ID del comprobante (partida)
     * @return Mono con el ID de la orden de ingreso creada
     */
    Mono<Integer> crearOrdenIngreso(Integer idCliente, Integer idAlmacen);

    /**
     * Crea un detalle de orden de ingreso
     * 
     * @param idOrdenIngreso ID de la orden de ingreso
     * @param idArticulo     ID del artículo
     * @param idUnidad       ID de la unidad
     * @param pesoRef        Peso de referencia
     * @param nuRollos       Número de rollos
     * @param idComprobante  ID del comprobante
     * @return Mono con el ID del detalle creado
     */
    Mono<Integer> crearDetalleOrdenIngreso(Integer idOrdenIngreso, Integer idArticulo, Integer idUnidad,
            java.math.BigDecimal pesoRef, java.math.BigDecimal nuRollos, Integer idComprobante);

    /**
     * Crea un detalle de peso de orden de ingreso
     * 
     * @param idOrdenIngreso    ID de la orden de ingreso
     * @param codRollo          Código del rollo
     * @param pesoRollo         Peso del rollo
     * @param idDetOrdenIngreso ID del detalle de orden de ingreso
     * @param idRolloIngreso    ID del rollo de ingreso
     * @return Mono con el ID del detalle de peso creado
     */
    Mono<Integer> crearDetallePesoOrdenIngreso(Integer idOrdenIngreso, String codRollo, java.math.BigDecimal pesoRollo,
            Integer idDetOrdenIngreso, Integer idRolloIngreso);

    /**
     * Actualiza el status de un detalle de peso de orden de ingreso a 0 (inactivo)
     * 
     * @param idDetOrdenIngresoPeso ID del detalle de peso a actualizar
     * @return Mono<Void> indicando el éxito de la operación
     */
    Mono<Void> actualizarStatusDetallePeso(Integer idDetOrdenIngresoPeso);

    /**
     * Obtiene la cantidad de rollos de una orden de ingreso
     * 
     * @param idOrdenIngreso ID de la orden de ingreso
     * @return Mono con la cantidad de rollos
     */
    Mono<Integer> getCantidadRollosOrdenIngreso(Integer idOrdenIngreso);

    /**
     * Deshabilita el detalle de ingreso de una orden
     * 
     * @param idOrdenIngreso ID de la orden de ingreso
     * @return Mono con el status actualizado
     */
    Mono<Integer> deshabilitarDetalleIngreso(Integer idOrdenIngreso);

    /**
     * Deshabilita la orden de ingreso completa
     * 
     * @param idOrdenIngreso ID de la orden de ingreso
     * @return Mono con el status actualizado
     */
    Mono<Integer> deshabilitarOrdenIngreso(Integer idOrdenIngreso);

    /**
     * Consulta la información completa de una orden de ingreso
     * 
     * @param idOrdenIngreso ID de la orden de ingreso
     * @return Mono con la información completa de la orden
     */
    Mono<OrdenIngresoCompletaProjection> consultarOrdenIngresoCompleta(Integer idOrdenIngreso);
}
