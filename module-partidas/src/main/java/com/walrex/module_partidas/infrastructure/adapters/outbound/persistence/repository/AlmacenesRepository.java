package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.OrdenIngresoCompletaProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Repository para operaciones CRUD en las tablas de almacenes
 * Maneja ordenes de ingreso, detalles y pesos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AlmacenesRepository {

    private final DatabaseClient databaseClient;

    /**
     * Crea una nueva orden de ingreso
     * 
     * @param idCliente     ID del cliente
     * @param idAlmacen     ID del almacén destino
     * @param idComprobante ID del comprobante (partida)
     * @return Mono con el ID de la orden creada
     */
    public Mono<Integer> crearOrdenIngreso(Integer idCliente, Integer idAlmacen) {
        String sql = """
                INSERT INTO almacenes.ordeningreso (id_cliente, id_almacen, fec_ingreso, status) VALUES (:idCliente, :idAlmacen, :fecIngreso, 1) RETURNING *
                """;

        log.debug("Creando orden de ingreso para cliente: {}, almacén: {}",
                idCliente, idAlmacen);

        return databaseClient.sql(sql)
                .bind("idCliente", idCliente)
                .bind("idAlmacen", idAlmacen)
                .bind("fecIngreso", LocalDate.now())
                .map((row, metadata) -> row.get("id_ordeningreso", Integer.class))
                .one()
                .doOnSuccess(id -> log.info("Orden de ingreso creada con ID: {}", id))
                .doOnError(error -> log.error("Error creando orden de ingreso: {}", error.getMessage()));
    }

    public Mono<Integer> crearOrdenIngresoRechazo(Integer idCliente, Integer idAlmacen, Integer idMotivoRechazo, String observacion) {
        String sql = """
                INSERT INTO almacenes.ordeningreso (id_cliente, id_almacen, fec_ingreso, id_motivo_rechazo, observacion, status) VALUES (:idCliente, :idAlmacen, :fecIngreso, :idMotivoRechazo, :observacion, 1) RETURNING *
                """;

        log.debug("Creando orden de ingreso para cliente: {}, almacén: {}", idCliente, idAlmacen);

        return databaseClient.sql(sql)
                .bind("idCliente", idCliente)
                .bind("idAlmacen", idAlmacen)
                .bind("fecIngreso", LocalDate.now())
                .bind("idMotivoRechazo", idMotivoRechazo)
                .bind("observacion", observacion)
                .map((row, metadata) -> row.get("id_ordeningreso", Integer.class))
                .one()
                .doOnSuccess(id -> log.info("Orden de ingreso creada con ID: {}", id))
                .doOnError(error -> log.error("Error creando orden de ingreso: {}", error.getMessage()));
    }

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
    public Mono<Integer> crearDetalleOrdenIngreso(Integer idOrdenIngreso, Integer idArticulo, Integer idUnidad,
            BigDecimal pesoRef, String lote, Integer nuRollos, Integer idComprobante) {
        String sql = """
                INSERT INTO almacenes.detordeningreso
                (id_ordeningreso, id_articulo, id_unidad, peso_alm, lote, nu_rollos, id_comprobante, status)
                VALUES (:idOrdenIngreso, :idArticulo, :idUnidad, :pesoRef, :lote, :nuRollos, :idComprobante, 1)
                RETURNING id_detordeningreso
                """;

        log.debug("Creando detalle de orden de ingreso para orden: {}, artículo: {}", idOrdenIngreso, idArticulo);

        return databaseClient.sql(sql)
                .bind("idOrdenIngreso", idOrdenIngreso)
                .bind("idArticulo", idArticulo)
                .bind("idUnidad", idUnidad)
                .bind("pesoRef", pesoRef)
                .bind("lote", lote)
                .bind("nuRollos", nuRollos)
                .bind("idComprobante", idComprobante)
                .map((row, metadata) -> row.get("id_detordeningreso", Integer.class))
                .one()
                .doOnSuccess(id -> log.info("Detalle de orden de ingreso creado con ID: {}", id))
                .doOnError(error -> log.error("Error creando detalle de orden de ingreso: {}", error.getMessage()));
    }

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
    public Mono<Integer> crearDetallePesoOrdenIngreso(Integer idOrdenIngreso, String codRollo, BigDecimal pesoRollo,
            Integer idDetOrdenIngreso, Integer idRolloIngreso) {
        String sql = """
                INSERT INTO almacenes.detordeningresopeso
                (id_ordeningreso, cod_rollo, peso_rollo, id_detordeningreso, id_rollo_ingreso, status, create_at, update_at)
                VALUES (:idOrdenIngreso, :codRollo, :pesoRollo, :idDetOrdenIngreso, :idRolloIngreso, 1, :createAt, :updateAt)
                RETURNING id_detordeningresopeso
                """;

        log.debug("Creando detalle de peso para orden: {}, rollo: {}", idOrdenIngreso, codRollo);

        OffsetDateTime now = OffsetDateTime.now();
        return databaseClient.sql(sql)
                .bind("idOrdenIngreso", idOrdenIngreso)
                .bind("codRollo", codRollo)
                .bind("pesoRollo", pesoRollo)
                .bind("idDetOrdenIngreso", idDetOrdenIngreso)
                .bind("idRolloIngreso", idRolloIngreso)
                .bind("createAt", now)
                .bind("updateAt", now)
                .map((row, metadata) -> row.get("id_detordeningresopeso", Integer.class))
                .one()
                .doOnSuccess(id -> log.info("Detalle de peso creado con ID: {}", id))
                .doOnError(error -> log.error("Error creando detalle de peso: {}", error.getMessage()));
    }

    public Mono<Integer> getCantidadRollosOrdenIngreso(Integer idOrdenIngreso){
        String sql = """
                SELECT COUNT(DISTINCT det_ing_pes.id_detordeningresopeso) AS cnt_rollos
                FROM almacenes.detordeningreso AS det_ing
                LEFT OUTER JOIN almacenes.detordeningresopeso det_ing_pes ON det_ing_pes.id_detordeningreso = det_ing.id_detordeningreso
                WHERE det_ing.id_ordeningreso = :idOrdenIngreso AND det_ing_pes.status = 1
                """;
        return databaseClient.sql(sql)
                .bind("idOrdenIngreso", idOrdenIngreso)
                .map((row, metadata) -> row.get("cnt_rollos", Integer.class))
                .one()
                .doOnSuccess(cantidad -> log.info("Detalle de ingreso consultado con ID: {}, cantidad de rollos: {}", idOrdenIngreso, cantidad))
                .doOnError(error -> log.error("Error consultando detalle de ingreso: {}", error.getMessage()));
    }


    public Mono<Integer> deshabilitarOrdenIngreso(Integer idOrdenIngreso){
        String sql = """
                UPDATE almacenes.ordeningreso SET status = 0 WHERE id_ordeningreso = :idOrdenIngreso RETURNING status
                """;
        return databaseClient.sql(sql)
                .bind("idOrdenIngreso", idOrdenIngreso)
                .map((row, metadata) -> row.get("status", Integer.class))
                .one()
                .doOnSuccess(status -> log.info("Orden de ingreso deshabilitado con ID: {}, status actualizado: {}", idOrdenIngreso, status))
                .doOnError(error -> log.error("Error deshabilitando orden de ingreso: {}", error.getMessage()));
    }

    public Mono<Integer> deshabilitarDetalleIngreso(Integer idOrdenIngreso){
        String sql = """
                UPDATE almacenes.detordeningresopeso SET status = 0 WHERE id_ordeningreso = :idOrdenIngreso RETURNING status
                """;
        return databaseClient.sql(sql)
                .bind("idOrdenIngreso", idOrdenIngreso)
                .map((row, metadata) -> row.get("status", Integer.class))
                .one()
                .doOnSuccess(status -> log.info("Detalle de ingreso deshabilitado con ID: {}, status actualizado: {}", idOrdenIngreso, status))
                .doOnError(error -> log.error("Error deshabilitando detalle de ingreso: {}", error.getMessage()));
    }
    /**
     * Actualiza el status de un detalle de peso de orden de ingreso a 0 (inactivo)
     * 
     * @param idDetOrdenIngresoPeso ID del detalle de peso a actualizar
     * @return Mono<Void> indicando el éxito de la operación
     */
    public Mono<Void> actualizarStatusDetallePeso(Integer idDetOrdenIngresoPeso) {
        String sql = """
                UPDATE almacenes.detordeningresopeso SET status = 0 WHERE id_detordeningresopeso = :idDetOrdenIngresoPeso
                """;

        log.debug("Actualizando status del detalle de peso ID: {}", idDetOrdenIngresoPeso);

        return databaseClient.sql(sql)
                .bind("idDetOrdenIngresoPeso", idDetOrdenIngresoPeso)
                .then()
                .doOnSuccess(v -> log.info("Status del detalle de peso ID {} actualizado a 0", idDetOrdenIngresoPeso))
                .doOnError(error -> log.error("Error actualizando status del detalle de peso ID {}: {}",
                        idDetOrdenIngresoPeso, error.getMessage()));
    }

    /**
     * Consulta la información completa de una orden de ingreso
     * 
     * @param idOrdenIngreso ID de la orden de ingreso
     * @return Mono con la información completa de la orden
     */
    public Mono<OrdenIngresoCompletaProjection> consultarOrdenIngresoCompleta(Integer idOrdenIngreso) {
        String sql = """
                SELECT id_ordeningreso, id_cliente, cod_ingreso, id_almacen
                FROM almacenes.ordeningreso 
                WHERE id_ordeningreso = :idOrdenIngreso
                """;
        return databaseClient.sql(sql)
                .bind("idOrdenIngreso", idOrdenIngreso)
                .map((row, metadata) -> new OrdenIngresoCompletaProjection(
                        row.get("id_ordeningreso", Integer.class),
                        row.get("id_cliente", Integer.class),
                        row.get("cod_ingreso", String.class),
                        row.get("id_almacen", Integer.class)
                ))
                .one()
                .doOnSuccess(orden -> log.info("Orden de ingreso consultada: {}", orden))
                .doOnError(error -> log.error("Error consultando orden de ingreso ID {}: {}", 
                        idOrdenIngreso, error.getMessage()));
    }
}
