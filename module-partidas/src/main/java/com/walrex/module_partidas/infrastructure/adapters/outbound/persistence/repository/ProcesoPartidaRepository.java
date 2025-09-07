package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.ProcesoPartidaProjection;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Repository para consultas de procesos de partida usando R2dbcTemplate
 * Ejecuta consultas SQL complejas con múltiples JOINs para obtener información
 * completa
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProcesoPartidaRepository {

    private final DatabaseClient databaseClient;

    /**
     * Consulta todos los procesos de una partida específica
     * Incluye información completa sobre el estado, máquinas y rutas
     * 
     * @param idPartida ID de la partida a consultar
     * @return Flux de proyecciones ProcesoPartidaProjection
     */
    public Flux<ProcesoPartidaProjection> findProcesosByPartida(Integer idPartida) {
        String sql = """
                SELECT tp.id_cliente, tp.id_partida, ppm.id_partida_maquina
                , tp2.id_ruta, tp2.id_articulo
                , COALESCE(ppm.id_proceso, t.id_proceso) AS id_proceso
                , COALESCE(ppm.id_det_ruta, t.id_det_ruta) AS id_det_ruta
                , t2.no_proceso, t2.id_almacen
                , ppm.id_maquina, ppm.id_tipo_maquina
                , CASE WHEN ppm.fec_real_inicio IS NULL THEN FALSE ELSE TRUE END AS iniciado
                , CASE WHEN ppm.fec_real_fin IS NULL THEN FALSE ELSE TRUE END AS finalizado
                , CASE WHEN COALESCE(ppm.status,0)=0 AND ppm.id_nivel_observ IS NULL THEN TRUE ELSE FALSE END AS is_pendiente
                , ppm.status, CASE WHEN COALESCE(ppm.is_main_proceso, 0) = 1 THEN TRUE ELSE FALSE END AS is_main_proceso
                , t3.desc_maq
                FROM produccion.tb_partidas tp
                LEFT OUTER JOIN comercial.tborden_produccion tp2 ON tp2.id_ordenproduccion = tp.id_ordenproduccion
                LEFT OUTER JOIN comercial.tbdetrutas t ON t.id_ruta = tp2.id_ruta
                LEFT OUTER JOIN comercial.tbprocesos t2 ON t2.id_proceso = t.id_proceso
                LEFT OUTER JOIN produccion.partidas_procesos_maquinas ppm ON ppm.id_partida = tp.id_partida AND (ppm.id_det_ruta=t.id_det_ruta OR ppm.id_proceso=t.id_proceso) AND ppm.is_adicional =0
                LEFT OUTER JOIN catalogo.tbmaquina t3 ON t3.id_maquina = ppm.id_maquina
                WHERE tp.id_partida = :idPartida
                ORDER BY t.id_det_ruta ASC
                """;

        log.debug("Ejecutando consulta de procesos para partida ID: {}", idPartida);

        return databaseClient.sql(sql)
                .bind("idPartida", idPartida)
                .map(this::mapToProcesoPartidaProjection)
                .all()
                .doOnComplete(() -> log.info("Consulta de procesos completada para partida ID: {}", idPartida))
                .doOnError(error -> log.error("Error consultando procesos para partida ID {}: {}", idPartida,
                        error.getMessage()));
    }

    /**
     * Mapea una fila de resultado a ProcesoPartidaProjection
     * 
     * @param row      Fila de resultado de la consulta
     * @param metadata Metadatos de la fila
     * @return Proyección del proceso de partida
     */
    private ProcesoPartidaProjection mapToProcesoPartidaProjection(Row row, RowMetadata metadata) {
        return ProcesoPartidaProjection.builder()
                .idCliente(row.get("id_cliente", Integer.class))
                .idPartida(row.get("id_partida", Integer.class))
                .idPartidaMaquina(row.get("id_partida_maquina", Integer.class))
                .idRuta(row.get("id_ruta", Integer.class))
                .idArticulo(row.get("id_articulo", Integer.class))
                .idProceso(row.get("id_proceso", Integer.class))
                .idDetRuta(row.get("id_det_ruta", Integer.class))
                .noProceso(row.get("no_proceso", String.class))
                .idAlmacen(row.get("id_almacen", Integer.class))
                .idMaquina(row.get("id_maquina", Integer.class))
                .idTipoMaquina(row.get("id_tipo_maquina", Integer.class))
                .iniciado(row.get("iniciado", Boolean.class))
                .finalizado(row.get("finalizado", Boolean.class))
                .isPendiente(row.get("is_pendiente", Boolean.class))
                .status(row.get("status", Integer.class))
                .isMainProceso(row.get("is_main_proceso", Boolean.class))
                .descMaq(row.get("desc_maq", String.class))
                .build();
    }
}
