package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Repository especializado para búsquedas básicas de conductores por documento y tipo de documento.
 * Implementa el patrón Repository con queries específicas y optimizadas.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConductorBasicSearchRepository {

    private final DatabaseClient databaseClient;

    /**
     * Busca conductores por número de documento y tipo de documento.
     * Query optimizada para búsquedas específicas.
     */
    public Flux<ConductorDataDTO> buscarConductorPorDocumento(String numDoc, Integer idTipDoc) {
        log.info("🔍 Ejecutando búsqueda básica de conductor - Documento: {}, Tipo: {}", numDoc, idTipDoc);

        String query = """
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
                AND c.num_documento LIKE :numDoc || '%'
                AND c.id_tipo_doc = :idTipDoc
                ORDER BY c.apellidos, c.nombres
                """;

        return databaseClient.sql(query)
                .bind("numDoc", numDoc)
                .bind("idTipDoc", idTipDoc)
                .map((row, metadata) -> mapRowToConductor(row))
                .all()
                .doOnNext(conductor -> log.info("✅ Conductor encontrado en BD: {} {}",
                        conductor.getNombres(), conductor.getApellidos()))
                .doOnError(error -> log.error("❌ Error en consulta de BD: {}", error.getMessage()));
    }

    /**
     * Mapea el resultado de la consulta al modelo de dominio usando projection.
     */
    private ConductorDataDTO mapRowToConductor(Row row) {
        return ConductorDataDTO.builder()
                .idConductor(row.get("id_conductor", Long.class))
                .numeroDocumento(trimOrNull(row.get("num_documento", String.class)))
                .apellidos(trimOrNull(row.get("apellidos", String.class)))
                .nombres(trimOrNull(row.get("nombres", String.class)))
                .numLicencia(trimOrNull(row.get("num_licencia", String.class)))
                .tipoDocumento(com.walrex.module_driver.domain.model.dto.TipoDocumentoDTO.builder()
                        .idTipoDocumento(row.get("id_tipodoc", Integer.class))
                        .descTipoDocumento(trimOrNull(row.get("no_tipodoc", String.class)))
                        .abrevTipoDocumento(trimOrNull(row.get("abrev_doc", String.class)))
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
