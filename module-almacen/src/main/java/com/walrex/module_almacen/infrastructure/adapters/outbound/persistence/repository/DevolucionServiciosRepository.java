package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import java.time.LocalDate;

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

        /**
         * Buscar devoluciones por ID de orden de salida habilitado
         */
        @Query("SELECT * FROM almacenes.devolucion_servicios WHERE id_ordensalida = :idOrdenSalida AND status = 1")
        Mono<DevolucionServiciosEntity> findByIdOrdenSalidaEnabled(Integer idOrdenSalida);

        /**
         * Actualizar devolución de servicios sin afectar timestamps
         * Los campos create_at y update_at se manejan automáticamente por la BD
         */
        @Query("UPDATE almacenes.devolucion_servicios SET " +
                        "motivo_comprobante = :idMotivoComprobante, " +
                        "id_comprobante = :idComprobante, " +
                        "id_empresa_transp = :idEmpresaTransp, " +
                        "id_modalidad = :idModalidad, " +
                        "id_conductor = :idConductor, " +
                        "num_placa = :numPlaca, " +
                        "id_llegada = :idLlegada, " +
                        "fec_entrega = :fecEntrega, " +
                        "id_usuario = :idUsuario " +
                        "WHERE id_devolucion = :idDevolucion " +
                        "RETURNING *")
        Mono<DevolucionServiciosEntity> updateDevolucionServicios(
                        Long idDevolucion,
                        Integer idMotivoComprobante,
                        Integer idComprobante,
                        Integer idEmpresaTransp,
                        Integer idModalidad,
                        Integer idConductor,
                        String numPlaca,
                        Integer idLlegada,
                        LocalDate fecEntrega,
                        Integer idUsuario);

        @Query("UPDATE almacenes.devolucion_servicios SET id_comprobante = :comprobanteId WHERE id_ordensalida = :idOrdenSalida AND status = 1")
        Mono<Integer> actualizarIdComprobante(Integer idComprobante, Integer idOrdenSalida);
}
