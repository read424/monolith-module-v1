package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionServiciosEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para DevolucionServiciosEntity
 * Maneja operaciones CRUD para almacenes.devolucion_servicios
 */
@Repository
public interface DevolucionServiciosRepository extends R2dbcRepository<DevolucionServiciosEntity, Long> {

       /**
        * Buscar devoluciones por ID de orden de salida
        */
       Mono<DevolucionServiciosEntity> findByIdOrdenSalida(Integer idOrdenSalida);

       /**
        * Buscar devoluciones por ID de motivo
        */
       Flux<DevolucionServiciosEntity> findByIdMotivo(Integer idMotivo);

       /**
        * Buscar devoluciones por ID de usuario
        */
       Flux<DevolucionServiciosEntity> findByIdUsuario(Integer idUsuario);

       /**
        * Buscar devoluciones por número de placa
        */
       Flux<DevolucionServiciosEntity> findByNumPlaca(String numPlaca);

       /**
        * Buscar devoluciones por número de documento del chofer
        */
       Flux<DevolucionServiciosEntity> findByNumDocChofer(String numDocChofer);

       /**
        * Consulta personalizada para obtener devoluciones con información adicional
        */
       @Query("SELECT ds.*, os.cod_salida, m.no_motivo " +
                     "FROM almacenes.devolucion_servicios ds " +
                     "INNER JOIN almacenes.ordensalida os ON ds.id_ordensalida = os.id_ordensalida " +
                     "INNER JOIN almacenes.tbmotivos m ON ds.id_motivo = m.id_motivo " +
                     "WHERE ds.id_devolucion = :idDevolucion")
       Mono<DevolucionServiciosEntity> findByIdWithDetails(Long idDevolucion);

       /**
        * Consulta para obtener devoluciones por rango de fechas
        */
       @Query("SELECT * FROM almacenes.devolucion_servicios " +
                     "WHERE create_at >= :fechaInicio AND create_at <= :fechaFin " +
                     "ORDER BY create_at DESC")
       Flux<DevolucionServiciosEntity> findByFechaRange(String fechaInicio, String fechaFin);

       /**
        * Verificar si existe una devolución para una orden de salida específica
        */
       @Query("SELECT COUNT(*) > 0 FROM almacenes.devolucion_servicios WHERE id_ordensalida = :idOrdenSalida")
       Mono<Boolean> existsByIdOrdenSalida(Integer idOrdenSalida);

}