package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.domain.model.dto.ItemProcessProductionDTO;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.DetailProcesoPartidaProjection;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartidaProcesosRepository {

    private final DatabaseClient databaseClient;


    public Mono<Boolean> changeStatusRollInStore(Integer idDetOrdenIngresoPeso, Integer status){
        String sql = """
            UPDATE almacenes.detordeningresopeso SET status=:newStatus WHERE id_detordeningresopeso=:idDetOrdenIngresoPeso AND status<>:newStatus;
            """;

        return databaseClient.sql(sql)
            .bind("newStatus", status)
            .bind("idDetOrdenIngresoPeso", idDetOrdenIngresoPeso)
            .fetch()
            .rowsUpdated()
            .map( rowsAffected -> rowsAffected > 0 );
    }



    public Flux<DetailProcesoPartidaProjection> findProcesosIncompletos(Integer idPartida){
        String sql= """
            SELECT tp.id_partida, t.id_det_ruta, t.id_proceso, t2.no_proceso, t2.id_tipo_maquina
            , t2.isservicio, ppm.id_partida_maquina, ppm.id_maquina, ppm.fec_real_inicio, ppm.hora_inicio
            , ppm.fec_real_fin, ppm.hora_fin, ppm.id_nivel_observ, ppm.is_main_proceso, ppm.status, ppm.observacion
            FROM produccion.tb_partidas tp
            LEFT OUTER JOIN comercial.tborden_produccion tp2 ON tp2.id_ordenproduccion = tp.id_ordenproduccion
            LEFT OUTER JOIN comercial.tbdetrutas t ON t.id_ruta = tp2.id_ruta
            LEFT OUTER JOIN comercial.tbprocesos t2 ON t2.id_proceso = t.id_proceso
            LEFT OUTER JOIN comercial.tb_ruta_procesos_tipo_reprocesos trptr ON trptr.id_ruta  = t.id_ruta AND trptr.id_det_ruta = t.id_det_ruta AND tp.type_reprocess = trptr.type_reprocess
            LEFT OUTER JOIN produccion.partidas_procesos_maquinas ppm ON ppm.id_partida = tp.id_partida AND ppm.id_proceso = t.id_proceso AND ppm.anulado=0
            WHERE tp.id_partida = :idPartida AND CASE WHEN tp.type_reprocess IS NOT NULL THEN trptr.type_reprocess=tp.type_reprocess ELSE TRUE END
            ORDER BY det_rut.id_det_ruta ASC
            """;

        return databaseClient.sql(sql)
            .bind("idPartida", idPartida)
            .map((row, metadata) -> mapToProjection(row))
            .all()
            .doOnComplete(()-> log.info("Consulta de procesos maquinas encontradas"));
    }

    /**
     * Guarda un proceso incompleto asociado a una partida.
     *
     * @param proceso   Datos del proceso a guardar
     * @param idPartida ID de la partida asociada
     * @return Mono con el ID del registro insertado
     */
    public Mono<Integer> saveProcesoIncompleto(ItemProcessProductionDTO proceso, Integer idPartida) {
        String sql = """
            INSERT INTO produccion.partidas_procesos_maquinas
            (id_partida, id_det_ruta, id_proceso, id_tipo_maquina, id_maquina, anulado, id_)
            VALUES (:idPartida, :idDetRuta, :idProceso, :idTipoMaquina, :idMaquina, 0)
            RETURNING id_partida_maquina
            """;

        return databaseClient.sql(sql)
            .bind("idPartida", idPartida)
            .bind("idDetRuta", proceso.getIdDetRuta())
            .bind("idProceso", proceso.getIdProceso())
            .bind("idTipoMaquina", proceso.getIdTipoMaquina())
            .bind("idMaquina", proceso.getIdMaquina())
            .map(row -> row.get("id_partida_maquina", Integer.class))
            .one()
            .doOnSuccess(id -> log.info("Proceso incompleto guardado con ID: {}", id))
            .doOnError(error -> log.error("Error guardando proceso incompleto: {}", error.getMessage()));
    }

    /**
     * Busca el primer ID de máquina disponible según el tipo de máquina.
     *
     * @param idTipoMaquina ID del tipo de máquina
     * @return Mono con el ID de la máquina encontrada
     */
    public Mono<Integer> findFirstIdMachineByTipoMaquina(Integer idTipoMaquina) {
        String sql = """
            SELECT id_maquina
            FROM comercial.tbmaquinas
            WHERE id_tipo_maquina = :idTipoMaquina
            AND activo = 1
            ORDER BY id_maquina ASC
            LIMIT 1
            """;

        return databaseClient.sql(sql)
            .bind("idTipoMaquina", idTipoMaquina)
            .map(row -> row.get("id_maquina", Integer.class))
            .one()
            .doOnSuccess(id -> log.info("Máquina encontrada con ID: {} para tipo: {}", id, idTipoMaquina))
            .doOnError(error -> log.error("Error buscando máquina para tipo {}: {}", idTipoMaquina, error.getMessage()));
    }

    private DetailProcesoPartidaProjection mapToProjection(Row row){
        try {
            return DetailProcesoPartidaProjection.builder()
                .id_partida(row.get("id_partida", Integer.class))
                .id_det_ruta(row.get("id_det_ruta", Integer.class))
                .id_proceso(row.get("id_proceso", Integer.class))
                .no_proceso(row.get("no_proceso", String.class))
                .id_tipo_maquina(row.get("id_tipo_maquina", Integer.class))
                .id_partida_maquina(row.get("id_partida_maquina", Integer.class))
                .id_maquina(row.get("id_maquina", Integer.class))
                .fec_real_inicio(row.get("fec_real_inicio", LocalDate.class))
                .hora_inicio(row.get("hora_inicio", String.class))
                .fec_real_fin(row.get("fec_real_fin", LocalDate.class))
                .hora_fin(row.get("hora_fin", String.class))
                .id_nivel_observ(row.get("id_nivel_observ", Integer.class))
                .is_main_proceso(row.get("is_main_proceso", Integer.class))
                .status(row.get("status", Integer.class))
                .observacion(row.get("observacion", String.class))
                .build();
        }catch(Exception e){
            throw new RuntimeException("Error en mapeo de proyeccion", e);
        }
    }
}
