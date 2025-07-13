package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.repository;

import java.math.BigDecimal;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.DetalleComprobanteEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad DetalleComprobanteEntity.
 * 
 * Proporciona operaciones CRUD reactivas y consultas personalizadas
 * para el manejo de detalles de comprobantes electrónicos.
 * 
 * CARACTERÍSTICAS:
 * - R2DBC Reactive Repository
 * - Operaciones asíncronas con Mono/Flux
 * - Consultas de agregación para totales
 * - Optimizado para consultas de detalle
 */
@Repository
public interface DetalleComprobantesRepository extends R2dbcRepository<DetalleComprobanteEntity, Long> {

    /**
     * Busca todos los detalles de un comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Flux de detalles del comprobante
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante " +
            "ORDER BY id_det_comprobante ASC")
    Flux<DetalleComprobanteEntity> findByIdComprobante(@Param("idComprobante") Long idComprobante);

    /**
     * Cuenta la cantidad de líneas de un comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el conteo de líneas
     */
    @Query("SELECT COUNT(*) FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante")
    Mono<Long> countByIdComprobante(@Param("idComprobante") Long idComprobante);

    /**
     * Busca detalles por producto.
     * 
     * @param idProducto ID del producto
     * @return Flux de detalles del producto
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE id_producto = :idProducto " +
            "ORDER BY create_at DESC")
    Flux<DetalleComprobanteEntity> findByIdProducto(@Param("idProducto") Integer idProducto);

    /**
     * Busca detalles de un producto en un comprobante específico.
     * 
     * @param idComprobante ID del comprobante
     * @param idProducto    ID del producto
     * @return Flux de detalles del producto en el comprobante
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante " +
            "AND id_producto = :idProducto")
    Flux<DetalleComprobanteEntity> findByComprobanteAndProducto(
            @Param("idComprobante") Long idComprobante,
            @Param("idProducto") Integer idProducto);

    /**
     * Busca detalles vinculados a una orden de salida.
     * 
     * @param idOrdenSalida ID de la orden de salida
     * @return Flux de detalles vinculados a la orden
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE id_ordensalida = :idOrdenSalida " +
            "ORDER BY id_detalle_orden ASC")
    Flux<DetalleComprobanteEntity> findByIdOrdenSalida(@Param("idOrdenSalida") Integer idOrdenSalida);

    /**
     * Busca detalles por orden de salida y detalle específico.
     * 
     * @param idOrdenSalida  ID de la orden de salida
     * @param idDetalleOrden ID del detalle de la orden
     * @return Mono del detalle específico
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE id_ordensalida = :idOrdenSalida " +
            "AND id_detalle_orden = :idDetalleOrden")
    Mono<DetalleComprobanteEntity> findByOrdenSalidaAndDetalle(
            @Param("idOrdenSalida") Integer idOrdenSalida,
            @Param("idDetalleOrden") Integer idDetalleOrden);

    /**
     * Calcula el subtotal de un comprobante sumando todos sus detalles.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el subtotal calculado
     */
    @Query("SELECT COALESCE(SUM(subtotal), 0) " +
            "FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante")
    Mono<BigDecimal> calcularSubtotalComprobante(@Param("idComprobante") Long idComprobante);

    /**
     * Calcula la cantidad total de productos en un comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con la cantidad total
     */
    @Query("SELECT COALESCE(SUM(cantidad), 0) " +
            "FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante")
    Mono<BigDecimal> calcularCantidadTotalComprobante(@Param("idComprobante") Long idComprobante);

    /**
     * Calcula el peso total de un comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el peso total
     */
    @Query("SELECT COALESCE(SUM(peso * cantidad), 0) " +
            "FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante")
    Mono<BigDecimal> calcularPesoTotalComprobante(@Param("idComprobante") Long idComprobante);

    /**
     * Verifica si existe un detalle para un producto en un comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @param idProducto    ID del producto
     * @return Mono con true si existe
     */
    @Query("SELECT COUNT(*) > 0 " +
            "FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante " +
            "AND id_producto = :idProducto")
    Mono<Boolean> existsByComprobanteAndProducto(
            @Param("idComprobante") Long idComprobante,
            @Param("idProducto") Integer idProducto);

    /**
     * Verifica si existen detalles para una orden de salida.
     * 
     * @param idOrdenSalida ID de la orden de salida
     * @return Mono con true si existen detalles
     */
    @Query("SELECT COUNT(*) > 0 " +
            "FROM facturacion.tbdet_comprobantes " +
            "WHERE id_ordensalida = :idOrdenSalida")
    Mono<Boolean> existsByOrdenSalida(@Param("idOrdenSalida") Integer idOrdenSalida);

    /**
     * Busca detalles por tipo de servicio.
     * 
     * @param idTipoServicio ID del tipo de servicio
     * @return Flux de detalles del tipo de servicio
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE id_tipo_servicio = :idTipoServicio " +
            "ORDER BY create_at DESC")
    Flux<DetalleComprobanteEntity> findByIdTipoServicio(@Param("idTipoServicio") Short idTipoServicio);

    /**
     * Busca detalles de servicios (productos sin peso).
     * 
     * @return Flux de detalles de servicios
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE peso = 0 OR peso IS NULL " +
            "ORDER BY create_at DESC")
    Flux<DetalleComprobanteEntity> findServicios();

    /**
     * Busca detalles de productos físicos (con peso).
     * 
     * @return Flux de detalles de productos físicos
     */
    @Query("SELECT * FROM facturacion.tbdet_comprobantes " +
            "WHERE peso > 0 " +
            "ORDER BY create_at DESC")
    Flux<DetalleComprobanteEntity> findProductosFisicos();

    /**
     * Actualiza el subtotal de un detalle.
     * 
     * @param idDetalleComprobante ID del detalle
     * @param nuevoSubtotal        nuevo subtotal
     * @return Mono con el número de filas afectadas
     */
    @Query("UPDATE facturacion.tbdet_comprobantes " +
            "SET subtotal = :nuevoSubtotal, " +
            "update_at = CURRENT_TIMESTAMP " +
            "WHERE id_det_comprobante = :idDetalleComprobante")
    Mono<Integer> updateSubtotal(
            @Param("idDetalleComprobante") Long idDetalleComprobante,
            @Param("nuevoSubtotal") BigDecimal nuevoSubtotal);

    /**
     * Actualiza la cantidad y recalcula el subtotal.
     * 
     * @param idDetalleComprobante ID del detalle
     * @param nuevaCantidad        nueva cantidad
     * @return Mono con el número de filas afectadas
     */
    @Query("UPDATE facturacion.tbdet_comprobantes " +
            "SET cantidad = :nuevaCantidad, " +
            "subtotal = :nuevaCantidad * precio, " +
            "update_at = CURRENT_TIMESTAMP " +
            "WHERE id_det_comprobante = :idDetalleComprobante")
    Mono<Integer> updateCantidadYSubtotal(
            @Param("idDetalleComprobante") Long idDetalleComprobante,
            @Param("nuevaCantidad") BigDecimal nuevaCantidad);

    /**
     * Elimina todos los detalles de un comprobante.
     * 
     * @param idComprobante ID del comprobante
     * @return Mono con el número de filas eliminadas
     */
    @Query("DELETE FROM facturacion.tbdet_comprobantes " +
            "WHERE id_comprobante = :idComprobante")
    Mono<Integer> deleteByIdComprobante(@Param("idComprobante") Long idComprobante);
}