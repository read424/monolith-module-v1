package com.walrex.module_driver.infrastructure.adapters.outbound.persistence;

import java.util.ArrayList;
import java.util.List;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import com.walrex.module_driver.application.ports.output.ConductorPersistencePort;
import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;
import com.walrex.module_driver.domain.model.dto.TipoDocumentoDTO;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity.DriverEntity;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity.TipoDocumentoEntity;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adaptador de persistencia para la búsqueda de conductores con query dinámico.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConductorRepositoryAdapter implements ConductorPersistencePort {

    private final DatabaseClient databaseClient;

    @Override
    public Flux<ConductorDataDTO> buscarConductorPorDocumento(String numDoc, Integer idTipDoc) {
        log.info("🔍 Ejecutando búsqueda dinámica de conductor - Documento: {}, Tipo: {}", numDoc, idTipDoc);

        String query = buildDynamicQuery(numDoc, idTipDoc);
        var spec = databaseClient.sql(query);

        // Bindear parámetros dinámicamente
        spec = bindParameters(spec, numDoc, idTipDoc);

        return spec.map((row, metadata) -> mapRowToConductor(row))
                .all()
                .doOnNext(conductor -> log.info("✅ Conductor encontrado en BD: {} {}",
                        conductor.getNombres(), conductor.getApellidos()))
                .doOnError(error -> log.error("❌ Error en consulta de BD: {}", error.getMessage()));
    }

    /**
     * Construye la query dinámica basada en los parámetros proporcionados.
     */
    private String buildDynamicQuery(String numDoc, Integer idTipDoc) {
        StringBuilder query = new StringBuilder();
        List<String> conditions = new ArrayList<>();

        query.append("""
                SELECT
                    c.id_conductor,
                    c.num_documento,
                    c.apellidos,
                    c.nombres,
                    c.num_licencia,
                    td.id_tipodoc,
                    td.no_tipodoc,
                    td.abrev_doc
                FROM ventas.tb_conductor c
                LEFT JOIN rrhh.tctipo_doc td ON td.id_tipodoc = c.id_tipo_doc AND td.status = '1'
                WHERE c.status = '1'
                """);

        // Agregar condiciones dinámicamente
        if (numDoc != null && !numDoc.trim().isEmpty()) {
            conditions.add("c.num_documento LIKE :numDoc || '%'");
        }

        if (idTipDoc != null) {
            conditions.add("c.id_tipo_doc = :idTipDoc");
        }

        // Agregar condiciones a la query
        if (!conditions.isEmpty()) {
            query.append(" AND ").append(String.join(" AND ", conditions));
        }

        log.debug("🔧 Query dinámica construida: {}", query.toString());
        return query.toString();
    }

    /**
     * Bindea los parámetros a la query de forma dinámica.
     */
    private DatabaseClient.GenericExecuteSpec bindParameters(DatabaseClient.GenericExecuteSpec spec,
            String numDoc, Integer idTipDoc) {

        if (numDoc != null && !numDoc.trim().isEmpty()) {
            spec = spec.bind("numDoc", numDoc);
        }

        if (idTipDoc != null) {
            spec = spec.bind("idTipDoc", idTipDoc);
        }

        return spec;
    }

    /**
     * Mapea el resultado de la consulta al modelo de dominio usando las entidades.
     */
    private ConductorDataDTO mapRowToConductor(Row row) {
        // Mapear DriverEntity
        DriverEntity driverEntity = DriverEntity.builder()
                .idConductor(row.get("id_conductor", Long.class))
                .numDocumento(trimOrNull(row.get("num_documento", String.class)))
                .apellidos(trimOrNull(row.get("apellidos", String.class)))
                .nombres(trimOrNull(row.get("nombres", String.class)))
                .numLicencia(trimOrNull(row.get("num_licencia", String.class)))
                .build();

        // Mapear TipoDocumentoEntity
        TipoDocumentoEntity tipoDocumentoEntity = TipoDocumentoEntity.builder()
                .idTipoDocumento(row.get("id_tipodoc", Integer.class))
                .descTipoDocumento(trimOrNull(row.get("no_tipodoc", String.class)))
                .abrevDoc(trimOrNull(row.get("abrev_doc", String.class)))
                .build();

        // Mapear al modelo de dominio
        return ConductorDataDTO.builder()
                .idConductor(driverEntity.getIdConductor())
                .numeroDocumento(driverEntity.getNumDocumento())
                .apellidos(driverEntity.getApellidos())
                .nombres(driverEntity.getNombres())
                .numLicencia(driverEntity.getNumLicencia())
                .tipoDocumento(TipoDocumentoDTO.builder()
                        .idTipoDocumento(tipoDocumentoEntity.getIdTipoDocumento())
                        .descTipoDocumento(tipoDocumentoEntity.getDescTipoDocumento())
                        .abrevTipoDocumento(tipoDocumentoEntity.getAbrevDoc())
                        .build())
                .build();
    }

    /**
     * Aplica trim() a un String si no es null, si es null retorna null.
     */
    private String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }
}