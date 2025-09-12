package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;
import com.walrex.module_driver.domain.model.dto.SearchDriverByParameters;

import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Repository especializado para búsquedas dinámicas de conductores usando projections.
 * Implementa el patrón Repository con queries dinámicas optimizadas.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConductorSearchRepository {

    private final DatabaseClient databaseClient;

    /**
     * Busca conductores usando parámetros dinámicos con projection optimizada.
     */
    public Flux<ConductorDataDTO> buscarConductorPorParametros(SearchDriverByParameters searchDriverByParameters) {
        log.info("🔍 Ejecutando búsqueda dinámica de conductor - Documento: {}, Tipo: {} / Nombre: {}", 
                searchDriverByParameters.getNumDoc(), 
                searchDriverByParameters.getIdTipDoc(), 
                searchDriverByParameters.getName());

        String query = buildDynamicQuery(searchDriverByParameters);
        var spec = databaseClient.sql(query);

        // Bindear parámetros dinámicamente
        spec = bindParameters(spec, searchDriverByParameters);

        return spec.map((row, metadata) -> mapRowToConductor(row))
                .all()
                .doOnNext(conductor -> log.info("✅ Conductor encontrado en BD: {} {}",
                        conductor.getNombres(), conductor.getApellidos()))
                .doOnError(error -> log.error("❌ Error en consulta de BD: {}", error.getMessage()));
    }

    /**
     * Construye la query dinámica basada en los parámetros proporcionados.
     * Usa projection optimizada para mejor rendimiento.
     */
    private String buildDynamicQuery(SearchDriverByParameters searchDriverByParameters) {
        StringBuilder query = new StringBuilder();
        StringBuilder conditions = new StringBuilder();

        // Query base con projection optimizada
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

        // Construir condiciones dinámicamente
        boolean hasConditions = false;

        if (searchDriverByParameters.getNumDoc() != null && !searchDriverByParameters.getNumDoc().trim().isEmpty()) {
            conditions.append(" AND c.num_documento LIKE :numDoc || '%'");
            hasConditions = true;
        }

        if (searchDriverByParameters.getIdTipDoc() != null && !searchDriverByParameters.getIdTipDoc().equals(0)) {
            conditions.append(" AND c.id_tipo_doc = :idTipDoc");
            hasConditions = true;
        }

        if (searchDriverByParameters.getName() != null && !searchDriverByParameters.getName().trim().isEmpty()) {
            conditions.append(" AND UPPER(c.apellidos || ' ' || c.nombres) LIKE UPPER('%' || :name || '%')");
            hasConditions = true;
        }

        // Agregar condiciones si existen
        if (hasConditions) {
            query.append(conditions);
        }

        // Ordenamiento para consistencia
        query.append(" ORDER BY c.apellidos, c.nombres");

        log.debug("🔧 Query dinámica construida: {}", query.toString());
        return query.toString();
    }

    /**
     * Bindea los parámetros a la query de forma dinámica.
     */
    private DatabaseClient.GenericExecuteSpec bindParameters(DatabaseClient.GenericExecuteSpec spec,
            SearchDriverByParameters searchDriverByParameters) {

        if (searchDriverByParameters.getNumDoc() != null && !searchDriverByParameters.getNumDoc().trim().isEmpty()) {
            spec = spec.bind("numDoc", searchDriverByParameters.getNumDoc());
        }

        if (searchDriverByParameters.getIdTipDoc() != null && !searchDriverByParameters.getIdTipDoc().equals(0)) {
            spec = spec.bind("idTipDoc", searchDriverByParameters.getIdTipDoc());
        }

        if (searchDriverByParameters.getName() != null && !searchDriverByParameters.getName().trim().isEmpty()) {
            spec = spec.bind("name", searchDriverByParameters.getName());
        }

        return spec;
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
