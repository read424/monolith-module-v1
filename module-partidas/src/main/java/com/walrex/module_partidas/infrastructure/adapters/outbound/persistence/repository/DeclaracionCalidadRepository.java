package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_partidas.domain.model.DeclaracionCalidad;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeclaracionCalidadRepository {

    private final DatabaseClient databaseClient;

    private static final String INSERT_SQL = """
            INSERT INTO produccion.declaracion_calidad
            (id_ubicacion, fecha_declaracion, id_partida, id_maquina, id_auditor,
             nivel_critico, id_motivo_rechazo, is_observado, observacion, cnt_rollos, status)
            VALUES
            (:idUbicacion, :fechaDeclaracion, :idPartida, :idMaquina, :idAuditor,
             :nivelCritico, :idMotivoRechazo, :isObservado, :observacion, :cntRollos, :status)
            RETURNING id, id_ubicacion, fecha_declaracion, id_partida, id_maquina, id_auditor,
                      nivel_critico, id_motivo_rechazo, is_observado, observacion, cnt_rollos,
                      status, created_at, updated_at
            """;

    public Mono<DeclaracionCalidad> insert(DeclaracionCalidad d) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(INSERT_SQL)
                .bind("idUbicacion", d.getIdUbicacion())
                .bind("fechaDeclaracion", d.getFechaDeclaracion())
                .bind("idPartida", d.getIdPartida());

        spec = bindNullable(spec, "idMaquina", d.getIdMaquina(), Integer.class);
        spec = bindNullable(spec, "idAuditor", d.getIdAuditor(), Integer.class);
        spec = bindNullable(spec, "nivelCritico", d.getNivelCritico(), Integer.class);
        spec = bindNullable(spec, "idMotivoRechazo", d.getIdMotivoRechazo(), Integer.class);
        spec = spec.bind("isObservado", d.getIsObservado() != null ? d.getIsObservado() : 0);
        spec = bindNullable(spec, "observacion", d.getObservacion(), String.class);
        spec = bindNullable(spec, "cntRollos", d.getCntRollos(), Integer.class);
        spec = spec.bind("status", d.getStatus() != null ? d.getStatus() : 1);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    private <T> DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec, String name, T value, Class<T> type) {
        return value != null ? spec.bind(name, value) : spec.bindNull(name, type);
    }

    private DeclaracionCalidad mapRow(Row row) {
        return DeclaracionCalidad.builder()
                .id(row.get("id", Integer.class))
                .idUbicacion(row.get("id_ubicacion", Integer.class))
                .fechaDeclaracion(row.get("fecha_declaracion", LocalDate.class))
                .idPartida(row.get("id_partida", Integer.class))
                .idMaquina(row.get("id_maquina", Integer.class))
                .idAuditor(row.get("id_auditor", Integer.class))
                .nivelCritico(row.get("nivel_critico", Integer.class))
                .idMotivoRechazo(row.get("id_motivo_rechazo", Integer.class))
                .isObservado(row.get("is_observado", Integer.class))
                .observacion(row.get("observacion", String.class))
                .cntRollos(row.get("cnt_rollos", Integer.class))
                .status(row.get("status", Integer.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }
}
