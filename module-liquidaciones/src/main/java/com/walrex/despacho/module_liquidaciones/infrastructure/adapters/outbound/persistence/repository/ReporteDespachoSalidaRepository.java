package com.walrex.despacho.module_liquidaciones.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.despacho.module_liquidaciones.domain.model.ReporteDespachoSalida;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteDespachoSalidaRepository {

    private final DatabaseClient databaseClient;

    private static final String QUERY_BASE = """
        SELECT tl.id_liquidacion, tl.fec_liquidacion, tl.entregado,
               CASE WHEN t.id_tipodoc = 3 THEN t.no_razon ELSE trim(from t.no_apepat|| ' '||t.no_apemat) ||', '|| trim(t.no_nombres) END AS razon_social,
               tp.cod_partida, COUNT(tdpl.id_det_peso_liquidacion) AS cnt_rollos, tl.num_guia,
               SUM(d.peso_rollo) AS kg_ingreso, SUM(tdpl.peso_salida) AS kg_salida,
               ROUND(((SUM(d.peso_rollo) - SUM(tdpl.peso_salida)) / SUM(d.peso_rollo))::numeric, 2) AS porc_merma
        FROM ventas.tb_liquidaciones tl
        LEFT OUTER JOIN comercial.tbclientes t ON t.id_cliente = tl.id_cliente
        LEFT OUTER JOIN ventas.tb_det_liquidaciones tdl ON tdl.id_liquidacion = tl.id_liquidacion
        LEFT OUTER JOIN ventas.tb_det_peso_liquidaciones tdpl ON tdpl.id_det_liquidacion = tdl.id_det_liquidacion
        LEFT OUTER JOIN produccion.tb_detail_partida tdp ON tdp.id_det_partida = tdpl.id_det_partida
        LEFT OUTER JOIN almacenes.detordeningresopeso d ON d.id_detordeningresopeso = tdp.id_detordeningresopeso
        LEFT OUTER JOIN produccion.tb_partidas tp ON tp.id_partida = tdl.id_partida
        WHERE tl.status = 1 AND (tl.fec_liquidacion BETWEEN :fechaStart AND :fechaEnd)
        """;

    private static final String FILTER_ENTREGADO = " AND tl.entregado = :entregado ";

    private static final String FILTER_ID_CLIENTE = " AND tl.id_cliente = :idCliente ";

    private static final String GROUP_ORDER = """
        GROUP BY tl.id_liquidacion, t.id_cliente, tdl.id_det_liquidacion, tp.id_partida
        ORDER BY tl.date_at_update DESC
        """;

    public Flux<ReporteDespachoSalida> findByFechaRangeAndEntregado(LocalDate fechaStart, LocalDate fechaEnd, Integer entregado, Integer idCliente) {
        log.debug("Consultando reporte de despacho salidas para rango: {} - {} entregado: {} y idCliente {}", fechaStart, fechaEnd, entregado, idCliente);

        // Construir filtros opcionales y sus parámetros
        StringBuilder queryBuilder = new StringBuilder(QUERY_BASE);
        Map<String, Object> optionalParams = new HashMap<>();

        if (entregado != null) {
            queryBuilder.append(FILTER_ENTREGADO);
            optionalParams.put("entregado", entregado);
        }

        if (idCliente != null) {
            queryBuilder.append(FILTER_ID_CLIENTE);
            optionalParams.put("idCliente", idCliente);
        }

        queryBuilder.append(GROUP_ORDER);
        String query = queryBuilder.toString();

        // Crear executor con parámetros base
        DatabaseClient.GenericExecuteSpec queryExecutor = databaseClient.sql(query)
            .bind("fechaStart", fechaStart)
            .bind("fechaEnd", fechaEnd);

        // Agregar parámetros opcionales
        for (Map.Entry<String, Object> entry : optionalParams.entrySet()) {
            queryExecutor = queryExecutor.bind(entry.getKey(), entry.getValue());
        }

        return queryExecutor.map(this::mapToReporteDespachoSalida)
            .all()
            .doOnComplete(() -> log.debug("Consulta de reporte despacho salidas completada"))
            .doOnError(e -> log.error("Error consultando reporte despacho salidas: {}", e.getMessage()));
    }

    private ReporteDespachoSalida mapToReporteDespachoSalida(Row row, RowMetadata metadata) {
        return new ReporteDespachoSalida(
            row.get("id_liquidacion", Long.class),
            row.get("razon_social", String.class),
            row.get("fec_liquidacion", LocalDate.class),
            row.get("entregado", Short.class),
            row.get("cod_partida", String.class),
            row.get("cnt_rollos", Integer.class),
            row.get("num_guia", String.class),
            row.get("kg_ingreso", BigDecimal.class),
            row.get("kg_salida", BigDecimal.class),
            row.get("porc_merma", BigDecimal.class)
        );
    }
}
