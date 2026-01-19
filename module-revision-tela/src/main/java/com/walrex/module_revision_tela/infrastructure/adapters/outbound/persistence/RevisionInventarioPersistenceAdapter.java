package com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.walrex.module_revision_tela.application.ports.output.RevisionInventarioPort;
import com.walrex.module_revision_tela.domain.exceptions.DuplicateRevisionException;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailIngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.DetailRolloRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.entity.IngresoRevisionEntity;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RevisionInventarioProjection;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.DetailIngresoRevisionRepository;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.DetailRolloRevisionRepository;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.repository.RevisionInventarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para RevisionInventario
 * Maneja errores de duplicidad y violación de constraints
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionInventarioPersistenceAdapter implements RevisionInventarioPort {

    private final RevisionInventarioRepository revisionInventarioRepository;
    private final DetailIngresoRevisionRepository detailIngresoRevisionRepository;
    private final DetailRolloRevisionRepository detailRolloRevisionRepository;

    @Override
    public Flux<RevisionInventarioProjection> obtenerDatosRevision() {
        log.debug("Consultando datos para revisión de inventario");
        return revisionInventarioRepository.findDatosRevisionInventario()
            .doOnComplete(() -> log.debug("Consulta de datos de revisión completada"))
            .doOnError(error -> log.error("Error consultando datos de revisión: {}", error.getMessage()));
    }

    @Override
    public Mono<IngresoRevisionEntity> guardarIngresoRevision(IngresoRevisionEntity entity) {
        log.debug("Guardando ingreso revisión para orden: {}, periodo: {}",
            entity.getIdOrdeningreso(), entity.getIdPeriodo());
        return revisionInventarioRepository.save(entity)
            .onErrorMap(error -> {
                log.error("Error guardando ingreso revisión - orden: {}, periodo: {}, cliente: {}, fecha: {}",
                    entity.getIdOrdeningreso(), entity.getIdPeriodo(),
                    entity.getIdCliente(), entity.getFecIngreso());
                return mapearErrorDuplicidad(error);
            })
            .doOnSuccess(saved -> log.debug("Ingreso revisión guardado: id={}", saved.getIdRevision()));
    }

    @Override
    public Mono<DetailIngresoRevisionEntity> guardarDetailIngresoRevision(DetailIngresoRevisionEntity entity) {
        log.trace("Guardando detalle ingreso revisión: id_revision={}, id_detordeningreso={}, id_articulo={}",
            entity.getIdRevision(), entity.getIdDetordeningreso(), entity.getIdArticulo());
        return detailIngresoRevisionRepository.save(entity)
            .onErrorMap(error -> {
                log.error("Error guardando detalle ingreso - id_revision: {}, id_detordeningreso: {}, id_articulo: {}",
                    entity.getIdRevision(), entity.getIdDetordeningreso(), entity.getIdArticulo());
                return mapearErrorDuplicidad(error);
            })
            .doOnSuccess(saved -> log.trace("Detalle ingreso guardado: id={}", saved.getIdDetail()));
    }

    @Override
    public Mono<DetailRolloRevisionEntity> guardarDetailRolloRevision(DetailRolloRevisionEntity entity) {
        log.trace("Guardando detalle rollo revisión: id_detail={}, id_detordeningresopeso={}, id_partida={}",
            entity.getIdDetail(), entity.getIdDetordeningresopeso(), entity.getIdPartida());
        return detailRolloRevisionRepository.save(entity)
            .onErrorMap(error -> {
                log.error("Error guardando detalle rollo - id_detail: {}, id_detordeningreso: {}, id_detordeningresopeso: {}, id_partida: {}",
                    entity.getIdDetail(), entity.getIdDetordeningreso(),
                    entity.getIdDetordeningresopeso(), entity.getIdPartida());
                return mapearErrorDuplicidad(error);
            })
            .doOnSuccess(saved -> log.trace("Detalle rollo guardado: id={}", saved.getId()));
    }

    private Throwable mapearErrorDuplicidad(Throwable error) {
        if (error instanceof DataIntegrityViolationException) {
            String mensajeCompleto = obtenerMensajeCompleto(error);
            log.debug("Mensaje de error completo: {}", mensajeCompleto);

            if (mensajeCompleto != null && mensajeCompleto.contains("duplicate key")) {
                String mensajeDetallado = extraerDetallesError(mensajeCompleto);
                log.warn("Intento de duplicación detectado: {}", mensajeDetallado);
                return new DuplicateRevisionException(mensajeDetallado, error);
            }
        }
        return error;
    }

    /**
     * Obtiene el mensaje completo recorriendo toda la cadena de causas
     */
    private String obtenerMensajeCompleto(Throwable error) {
        StringBuilder mensajeCompleto = new StringBuilder();

        // Mensaje principal
        if (error.getMessage() != null) {
            mensajeCompleto.append(error.getMessage());
        }

        // Recorrer todas las causas
        Throwable causa = error.getCause();
        while (causa != null) {
            if (causa.getMessage() != null) {
                mensajeCompleto.append(" | ").append(causa.getMessage());
            }
            causa = causa.getCause();
        }

        return mensajeCompleto.toString();
    }

    /**
     * Extrae información detallada del error de PostgreSQL
     * Ejemplo de mensaje: "duplicate key value violates unique constraint \"ingreso_revision_unique\"
     * Detail: Key (id_ordeningreso, id_periodo)=(12345, 1) already exists."
     */
    private String extraerDetallesError(String mensajeCompleto) {
        try {
            // Extraer nombre del constraint
            String constraint = "desconocido";
            if (mensajeCompleto.contains("constraint \"")) {
                int inicio = mensajeCompleto.indexOf("constraint \"") + 12;
                int fin = mensajeCompleto.indexOf("\"", inicio);
                if (fin > inicio) {
                    constraint = mensajeCompleto.substring(inicio, fin);
                }
            } else if (mensajeCompleto.contains("constraint \\\"")) {
                // Algunas veces viene escapado
                int inicio = mensajeCompleto.indexOf("constraint \\\"") + 13;
                int fin = mensajeCompleto.indexOf("\\\"", inicio);
                if (fin > inicio) {
                    constraint = mensajeCompleto.substring(inicio, fin);
                }
            }

            // Extraer detalles de la clave duplicada - probar varios formatos
            String detalles = "";

            // Formato 1: "Detail: Key (...)"
            if (mensajeCompleto.contains("Detail:")) {
                int inicioDetail = mensajeCompleto.indexOf("Detail:") + 7;
                String resto = mensajeCompleto.substring(inicioDetail).trim();

                // Buscar hasta el final de la oración o salto de línea
                int finDetail = resto.indexOf(".");
                if (finDetail == -1) finDetail = resto.indexOf("\n");
                if (finDetail == -1) finDetail = resto.length();

                detalles = resto.substring(0, Math.min(finDetail, resto.length())).trim();
            }

            // Formato 2: "Key (...) already exists" (sin Detail:)
            else if (mensajeCompleto.contains("Key (") && mensajeCompleto.contains(") already exists")) {
                int inicioKey = mensajeCompleto.indexOf("Key (");
                int finKey = mensajeCompleto.indexOf(" already exists", inicioKey) + 15;
                if (inicioKey >= 0 && finKey > inicioKey) {
                    detalles = mensajeCompleto.substring(inicioKey, finKey);
                }
            }

            // Formato 3: Buscar pattern "(...) = (...)"
            else if (mensajeCompleto.matches(".*\\(.*\\)\\s*=\\s*\\(.*\\).*")) {
                int inicio = mensajeCompleto.indexOf("(");
                int fin = mensajeCompleto.lastIndexOf(")") + 1;
                if (inicio >= 0 && fin > inicio) {
                    detalles = "Valores: " + mensajeCompleto.substring(inicio, Math.min(fin, mensajeCompleto.length()));
                }
            }

            // Limpiar el mensaje si es muy largo
            if (detalles.length() > 200) {
                detalles = detalles.substring(0, 200) + "...";
            }

            // Construir mensaje legible
            if (!detalles.isEmpty()) {
                return String.format("Registro duplicado en constraint '%s'. %s", constraint, detalles);
            } else {
                return String.format("Registro duplicado en constraint '%s'. Revisa los logs para ver los datos duplicados.", constraint);
            }
        } catch (Exception e) {
            log.warn("Error parseando mensaje de duplicidad: {}", e.getMessage());
            return "Violación de constraint de unicidad: " + mensajeCompleto.substring(0, Math.min(300, mensajeCompleto.length()));
        }
    }
}
