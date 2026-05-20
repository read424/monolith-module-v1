package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.domain.model.dto.ReporteDeclaracionCalidadDTO;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteDeclaracionCalidadRepository {

    private final DatabaseClient databaseClient;

    private static final String REPORTE_SQL = """
            SELECT CASE WHEN t3.id_tipodoc=3 THEN t3.no_razon
                        ELSE TRIM(t3.no_apepat || t3.no_apemat ||', '|| t3.no_nombres)
                   END AS razon_social
                 , ppm.fec_programacion, ppm.fec_real_inicio, dc.fecha_declaracion, o.fec_ingreso
                 , tp.cod_partida || CASE WHEN tp.id_partida_parent IS NULL THEN '' ELSE '-R'||tp.num_reproceso END AS cod_partida
                 , t4.desc_articulo, t6.no_color, dc.cnt_rollos
                 , CASE WHEN dc.id_ubicacion=18 THEN 'TINTORERIA' ELSE 'ACABADO' END AS tipo_declaracion
                 , dc.is_observado, dc.observacion
                 , dc.nivel_critico, tmr.descripcion AS desc_motivo_rechazo
            FROM produccion.declaracion_calidad dc
            LEFT OUTER JOIN produccion.tb_partidas tp         ON tp.id_partida = dc.id_partida
            LEFT OUTER JOIN comercial.tborden_produccion tp2  ON tp2.id_ordenproduccion = tp.id_ordenproduccion
            LEFT OUTER JOIN almacenes.ordeningreso o          ON o.id_ordeningreso = tp2.id_ordeningreso
            LEFT OUTER JOIN comercial.tbrutas t7              ON t7.id_ruta = tp2.id_ruta
            LEFT OUTER JOIN logistica.tbarticulos t4          ON t4.id_articulo = tp2.id_articulo
            LEFT OUTER JOIN comercial.tbclientes t3           ON t3.id_cliente = tp.id_cliente
            LEFT OUTER JOIN produccion.partidas_procesos_maquinas ppm
                                                              ON ppm.id_partida = tp.id_partida AND ppm.is_main_proceso = 1
            LEFT OUTER JOIN catalogo.tbmaquina t              ON t.id_maquina = COALESCE(dc.id_maquina,
                                                                    CASE WHEN dc.id_ubicacion=18 THEN ppm.id_maquina ELSE NULL END)
            LEFT OUTER JOIN almacenes.tb_motivo_rechazo tmr   ON tmr.id = dc.id_motivo_rechazo
            LEFT OUTER JOIN rrhh.tbpersonal t2                ON t2.id_personal = dc.id_auditor
            LEFT OUTER JOIN laboratorio.tb_receta tr          ON tr.id_receta = tp.id_receta
            LEFT OUTER JOIN laboratorio.tbcolores t5          ON t5.id_colores = tr.id_colores
            LEFT OUTER JOIN laboratorio.tbcolor t6            ON t6.id_color = t5.id_color
            WHERE dc.fecha_declaracion = :fechaDeclaracion
            ORDER BY dc.fecha_declaracion ASC
            """;

    public Flux<ReporteDeclaracionCalidadDTO> findByFechaAndUbicacion(Integer idUbicacion, String fechaDeclaracion) {
        return databaseClient.sql(REPORTE_SQL)
                .bind("fechaDeclaracion", LocalDate.parse(fechaDeclaracion))
                .map((row, meta) -> mapRow(row))
                .all();
    }

    private ReporteDeclaracionCalidadDTO mapRow(Row row) {
        return ReporteDeclaracionCalidadDTO.builder()
                .razonSocial(row.get("razon_social", String.class))
                .fecProgramacion(row.get("fec_programacion", LocalDate.class))
                .fecRealInicio(row.get("fec_real_inicio", LocalDate.class))
                .fechaDeclaracion(row.get("fecha_declaracion", LocalDate.class))
                .fecIngreso(row.get("fec_ingreso", LocalDate.class))
                .codPartida(row.get("cod_partida", String.class))
                .descArticulo(row.get("desc_articulo", String.class))
                .noColor(row.get("no_color", String.class))
                .cntRollos(row.get("cnt_rollos", Integer.class))
                .tipoDeclaracion(row.get("tipo_declaracion", String.class))
                .nivelCritico(row.get("nivel_critico", Integer.class))
                .descMotivoRechazo(row.get("desc_motivo_rechazo", String.class))
                .isObservado(row.get("is_observado", Integer.class))
                .observacion(row.get("observacion", String.class))
                .build();
    }
}
