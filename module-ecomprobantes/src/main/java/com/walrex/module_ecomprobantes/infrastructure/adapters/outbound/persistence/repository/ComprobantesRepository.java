package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.repository;

import java.time.LocalDate;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.persistence.entity.ComprobanteEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad ComprobanteEntity.
 * 
 * Proporciona operaciones CRUD reactivas y consultas personalizadas
 * para el manejo de comprobantes electrónicos.
 * 
 * - Operaciones asíncronas con Mono/Flux
 * - Consultas personalizadas con @Query
 * - Métodos de búsqueda por diferentes criterios
 */
@Repository
public interface ComprobantesRepository extends R2dbcRepository<ComprobanteEntity, Long> {

        /**
         * Busca un comprobante por tipo, serie y número.
         * 
         * @param tipoComprobante   tipo de comprobante
         * @param tipoSerie         serie del comprobante
         * @param numeroComprobante número del comprobante
         * @return Mono del comprobante encontrado
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE id_tipocompro = :tipoComprobante " +
                        "AND tctipo_serie = :tipoSerie " +
                        "AND nro_comprobante = :numeroComprobante " +
                        "AND anulado = 0")
        Mono<ComprobanteEntity> findByTipoSerieNumero(
                        Integer tipoComprobante,
                        Integer tipoSerie,
                        Integer numeroComprobante);

        /**
         * Busca comprobantes por número de ticket.
         * 
         * @param numeroTicket número de ticket
         * @return Mono del comprobante encontrado
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE num_ticket = :numeroTicket " +
                        "AND anulado = 0")
        Mono<ComprobanteEntity> findByNumeroTicket(String numeroTicket);

        /**
         * Busca comprobantes activos por cliente.
         * 
         * @param idCliente ID del cliente
         * @return Flux de comprobantes del cliente
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE id_cliente = :idCliente " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY fe_emision DESC")
        Flux<ComprobanteEntity> findByClienteActivos(Integer idCliente);

        /**
         * Busca comprobantes por cliente en un rango de fechas.
         * 
         * @param idCliente   ID del cliente
         * @param fechaInicio fecha inicial
         * @param fechaFin    fecha final
         * @return Flux de comprobantes del cliente en el rango
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE id_cliente = :idCliente " +
                        "AND fe_emision BETWEEN :fechaInicio AND :fechaFin " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY fe_emision DESC")
        Flux<ComprobanteEntity> findByClienteAndFechaRange(
                        Integer idCliente,
                        LocalDate fechaInicio,
                        LocalDate fechaFin);

        /**
         * Busca comprobantes pendientes de envío a SUNAT.
         * 
         * @return Flux de comprobantes pendientes
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE cod_response_sunat IS NULL " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY fe_emision ASC")
        Flux<ComprobanteEntity> findPendientesEnvioSunat();

        /**
         * Busca comprobantes rechazados por SUNAT.
         * 
         * @return Flux de comprobantes rechazados
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE cod_response_sunat IS NOT NULL " +
                        "AND cod_response_sunat != 0 " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY fec_comunicacion DESC")
        Flux<ComprobanteEntity> findRechazadosPorSunat();

        /**
         * Busca comprobantes aceptados por SUNAT.
         * 
         * @return Flux de comprobantes aceptados
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE cod_response_sunat = 0 " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY fec_comunicacion DESC")
        Flux<ComprobanteEntity> findAceptadosPorSunat();

        /**
         * Busca comprobantes emitidos en una fecha específica.
         * 
         * @param fechaEmision fecha de emisión
         * @return Flux de comprobantes de la fecha
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE fe_emision = :fechaEmision " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY create_at DESC")
        Flux<ComprobanteEntity> findByFechaEmision(LocalDate fechaEmision);

        /**
         * Busca comprobantes en un rango de fechas.
         * 
         * @param fechaInicio fecha inicial
         * @param fechaFin    fecha final
         * @return Flux de comprobantes en el rango
         */
        @Query("SELECT * FROM facturacion.tbcomprobantes " +
                        "WHERE fe_emision BETWEEN :fechaInicio AND :fechaFin " +
                        "AND status = 1 AND anulado = 0 " +
                        "ORDER BY fe_emision DESC")
        Flux<ComprobanteEntity> findByRangoFechas(
                        LocalDate fechaInicio,
                        LocalDate fechaFin);

        /**
         * Cuenta comprobantes activos por cliente.
         * 
         * @param idCliente ID del cliente
         * @return Mono con el conteo
         */
        @Query("SELECT COUNT(*) FROM facturacion.tbcomprobantes " +
                        "WHERE id_cliente = :idCliente " +
                        "AND status = 1 AND anulado = 0")
        Mono<Long> countByClienteActivos(Integer idCliente);

        /**
         * Cuenta comprobantes pendientes de SUNAT.
         * 
         * @return Mono con el conteo
         */
        @Query("SELECT COUNT(*) FROM facturacion.tbcomprobantes " +
                        "WHERE cod_response_sunat IS NULL " +
                        "AND status = 1 AND anulado = 0")
        Mono<Long> countPendientesSunat();

        /**
         * Obtiene el siguiente número de comprobante para un tipo y serie.
         * 
         * @param tipoComprobante tipo de comprobante
         * @param tipoSerie       serie del comprobante
         * @return Mono con el siguiente número disponible
         */
        @Query("SELECT COALESCE(MAX(nro_comprobante), 0) + 1 " +
                        "FROM facturacion.tbcomprobantes " +
                        "WHERE id_tipocompro = :tipoComprobante " +
                        "AND tctipo_serie = :tipoSerie")
        Mono<Integer> getNextNumeroComprobante(
                        Integer tipoComprobante,
                        Integer tipoSerie);

        /**
         * Marca un comprobante como anulado.
         * 
         * @param idComprobante ID del comprobante
         * @return Mono con el número de filas afectadas
         */
        @Query("UPDATE facturacion.tbcomprobantes " +
                        "SET anulado = 1, update_at = CURRENT_TIMESTAMP " +
                        "WHERE id_comprobante = :idComprobante")
        Mono<Integer> anularComprobante(Long idComprobante);

        /**
         * Actualiza la respuesta de SUNAT.
         * 
         * @param idComprobante  ID del comprobante
         * @param codigoResponse código de respuesta SUNAT
         * @param responseSunat  mensaje de respuesta SUNAT
         * @return Mono con el número de filas afectadas
         */
        @Query("UPDATE facturacion.tbcomprobantes " +
                        "SET cod_response_sunat = :codigoResponse, " +
                        "response_sunat = :responseSunat, " +
                        "fec_comunicacion = CURRENT_DATE, " +
                        "update_at = CURRENT_TIMESTAMP " +
                        "WHERE id_comprobante = :idComprobante")
        Mono<Integer> updateResponseSunat(
                        Long idComprobante,
                        Integer codigoResponse,
                        String responseSunat);

        /**
         * Actualiza el comprobante con la respuesta completa de Lycet.
         * 
         * @param idComprobante       ID del comprobante
         * @param status              Status del comprobante (2=exitoso, 3=error)
         * @param codigoResponseSunat Código de respuesta de SUNAT (solo si hay error)
         * @param responseSunat       Mensaje de respuesta de SUNAT
         * @param numeroTicket        Número de ticket de SUNAT
         * @return Mono con el número de filas afectadas
         */
        @Query("UPDATE facturacion.tbcomprobantes " +
                        "SET status = :status, " +
                        "cod_response_sunat = :codigoResponseSunat, " +
                        "response_sunat = :responseSunat, " +
                        "fec_comunicacion = CURRENT_DATE, " +
                        "num_ticket = :numeroTicket, " +
                        "update_at = CURRENT_TIMESTAMP " +
                        "WHERE id_comprobante = :idComprobante")
        Mono<Integer> actualizarComprobanteConRespuestaLycet(
                        Long idComprobante,
                        Integer status,
                        Integer codigoResponseSunat,
                        String responseSunat,
                        String numeroTicket);
}