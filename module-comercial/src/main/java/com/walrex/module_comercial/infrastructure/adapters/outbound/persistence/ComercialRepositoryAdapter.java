package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence;

import com.walrex.module_comercial.application.ports.output.ComercialRepositoryPort;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.OrdenProduccionPartidaDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.PartidasStatusDespachoDTO;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.ProcesosPartidaDTO;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Adapter para operaciones complejas de repositorio en el módulo comercial.
 * Implementa consultas SQL personalizadas usando R2dbcEntityTemplate y DatabaseClient.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ComercialRepositoryAdapter implements ComercialRepositoryPort {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<OrdenProduccionPartidaDTO> getInfoOrdenProduccionPartida(Integer idPartida){
        String sql= """
                SELECT tp.id_partida, tp.condition, tp.status, tp.id_ordenproduccion, tp2.cod_ordenproduccion, tp2.id_orden, tp2.id_det_os
                , tos.id_gama, tp.id_ruta, tp.id_ordeningreso
                , tos.id_precio, tos.precio
                , t.desc_articulo || ' (A: ' || CAST(tp2.ancho AS numeric)|| ' R: '|| CAST(tp2.rendimiento AS numeric) || ' D:'|| CAST(tp2.densidad AS numeric)||') '||t2.desc_modo|| CASE WHEN CASE WHEN tp2.id_encogimiento=0 THEN NULL ELSE tp2.id_encogimiento END IS NOT NULL THEN ' Encog: '|| COALESCE(t3.desc_encogimiento, '')||' (A) X '||CAST(tp2.encogimiento_largo AS numeric)||' (L)' ELSE '' END || CASE WHEN tp2.revirado='' THEN '' ELSE 'Rev: '||tp2.revirado END || CASE WHEN tp2.antipilling=1 THEN 'CON ANTIPILING' ELSE '' END AS desc_articulo
                FROM produccion.tb_partidas tp
                LEFT OUTER JOIN comercial.tborden_produccion tp2 ON tp2.id_ordenproduccion = tp.id_ordenproduccion
                LEFT OUTER JOIN comercial.tbdet_orden_servicio tos ON tos.id_det_os =tp2.id_det_os
                LEFT OUTER JOIN logistica.tbarticulos t ON t.id_articulo = tp2.id_articulo
                LEFT OUTER JOIN catalogo.tbmodo t2 ON t2.id_modo = tp2.id_tipo
                LEFT OUTER JOIN catalogo.tbencogimiento t3 ON t3.id_encogimiento = tp2.id_encogimiento
                WHERE tp.id_partida = :idPartida
            """;
        return databaseClient.sql(sql)
            .bind("idPartida", idPartida)
            .map((row, metadata) -> mapRowToOrdenProduccionDetail(row))
            .one()
            .doOnNext(ordenproduccion -> log.debug("Orden Produccion Partida: {}", ordenproduccion))
            .switchIfEmpty(Mono.fromRunnable(() ->
                log.warn("No se encontró orden de producción para partida: {}", idPartida)))
            .doOnError(error -> log.error("Error consultando orden produccion de partida {}: {}",
                idPartida, error.getMessage()));
    }

    @Override
    public Flux<ProcesosPartidaDTO> getProcesosPartidaStatus(Integer idPartida) {
        String sql = """
                    SELECT ppm.id_partida_maquina, tp2.id_ruta, ppm.id_proceso, t2.no_proceso, ppm.fec_real_inicio, ppm.fec_real_fin, ppm.status
                    , ppm.is_main_proceso
                    FROM produccion.tb_partidas tp
                    LEFT OUTER JOIN comercial.tborden_produccion tp2 ON tp2.id_ordenproduccion = tp.id_ordenproduccion
                    LEFT OUTER JOIN comercial.tbdetrutas t ON t.id_ruta = tp2.id_ruta\s
                    LEFT OUTER JOIN comercial.tbprocesos t2 ON t2.id_proceso = t.id_proceso
                    LEFT OUTER JOIN produccion.partidas_procesos_maquinas ppm ON ppm.id_partida = tp.id_partida AND ppm.id_det_ruta = t.id_det_ruta AND ppm.anulado =0
                    WHERE tp.id_partida = :idPartida
                    ORDER BY t.id_det_ruta ASC
                """;

        log.debug("Consultando status procesos de partida: {}", idPartida);

        return databaseClient.sql(sql)
                .bind("idPartida", idPartida)
                .map((row, metadata)-> mapRowToPartidaStatus(row))
                .all()
                .doOnNext(partida -> log.debug("Partida encontrada: {}", partida))
                .doOnComplete(() -> log.info("Consulta de partidas completada para orden: {}", idPartida))
                .doOnError(error -> log.error("Error consultando partidas para orden {}: {}",
                        idPartida, error.getMessage()));
    }

    @Override
    public Flux<PartidasStatusDespachoDTO> getStatusDespachoPartidas(Integer idOrdenProduccion){
        String sql ="""
                SELECT tp.id_partida, SUM(CASE WHEN tl.num_guia IS NULL THEN 0 ELSE 1 END) cnt_despachado
                FROM produccion.tb_partidas AS tp
                LEFT OUTER JOIN ventas.tb_det_liquidaciones tdl ON tdl.id_partida = tp.id_partida
                LEFT OUTER JOIN ventas.tb_liquidaciones tl ON tl.id_liquidacion = tdl.id_liquidacion
                WHERE tp.id_ordenproduccion = :idOrdenProduccion
                GROUP BY tp.id_partida
            """;

        log.info("🔍 [REPOSITORY] Consultando partidas status despacho para orden: {}", idOrdenProduccion);

        return databaseClient.sql(sql)
            .bind("idOrdenProduccion", idOrdenProduccion)
            .map((row, metadata)->mapRowToPartidaStatusDespacho(row))
            .all()
            .doOnSubscribe(sub -> log.info("📡 [REPOSITORY] Suscripción iniciada a getStatusDespachoPartidas"))
            .doOnNext(partida -> log.info("📦 [REPOSITORY] Partida encontrada: idPartida={}, despachado={}",
                    partida.getIdPartida(), partida.getCntDespachado()))
            .doOnComplete(() -> log.info("✅ [REPOSITORY] Consulta completada para orden: {}", idOrdenProduccion))
            .doOnError(error -> log.error("❌ [REPOSITORY] Error consultando partidas para orden {}: {}",
                idOrdenProduccion, error.getMessage(), error));
    }

    private PartidasStatusDespachoDTO mapRowToPartidaStatusDespacho(Row row){
        return PartidasStatusDespachoDTO.builder()
            .idPartida(row.get("id_partida", Integer.class))
            .cntDespachado(row.get("cnt_despachado", Integer.class))
            .build();
    }

    private ProcesosPartidaDTO mapRowToPartidaStatus(Row row) {
        return ProcesosPartidaDTO.builder()
                .idPartidaMaquina(row.get("id_partida_maquina", Integer.class))
                .idRuta(row.get("id_ruta", Integer.class))
                .idProceso(row.get("id_proceso", Integer.class))
                .noProceso(row.get("no_proceso", String.class))
                .fecRealInicio(row.get("fec_real_inicio", LocalDate.class))
                .fecRealFin(row.get("fec_real_fin", LocalDate.class))
                .status(row.get("status", Integer.class))
                .isMainProceso(row.get("is_main_proceso", Integer.class))
                .build();
    }

    private OrdenProduccionPartidaDTO mapRowToOrdenProduccionDetail(Row row) {
        return OrdenProduccionPartidaDTO.builder()
            .idPartida(row.get("id_partida", Integer.class))
            .condition(row.get("condition", Integer.class))
            .status(row.get("status", Integer.class))
            .idOrdenProduccion(row.get("id_ordenproduccion", Integer.class))
            .codOrdenProduccion(row.get("cod_ordenproduccion", String.class))
            .idOrden(row.get("id_orden", Integer.class))
            .idDetOs(row.get("id_det_os", Integer.class))
            .idRuta(row.get("id_ruta", Integer.class))
            .idGama(row.get("id_gama", Integer.class))
            .idOrdenIngreso(row.get("id_ordeningreso", Integer.class))
            .idPrecio(row.get("id_precio", Integer.class))
            .precio(row.get("precio", Double.class))
            .descArticulo(row.get("desc_articulo", String.class))
            .build();
    }
}
