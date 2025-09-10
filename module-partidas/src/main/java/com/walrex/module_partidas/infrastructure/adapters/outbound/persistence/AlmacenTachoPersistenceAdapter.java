package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_partidas.application.ports.output.ConsultarAlmacenTachoPort;
import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.domain.model.AlmacenTachoResponse;
import com.walrex.module_partidas.domain.model.dto.ConsultarAlmacenTachoRequest;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper.AlmacenTachoMapper;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.AlmacenTachoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.AlmacenTachoRepositoryAdapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para Almacen Tacho
 * Implementa el puerto de salida y se encarga de la comunicación con la base de
 * datos
 * Incluye el cálculo de milisegundos transcurridos desde el registro
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlmacenTachoPersistenceAdapter implements ConsultarAlmacenTachoPort {

    private final AlmacenTachoRepositoryAdapter repositoryAdapter;
    private final AlmacenTachoMapper mapper;

    @Override
    public Mono<AlmacenTachoResponse> consultarAlmacenTacho(ConsultarAlmacenTachoRequest request) {
        log.debug("Consultando almacén tacho para almacén ID: {} con paginación: page={}, size={}, codPartida={}",
            request.getIdAlmacen(), request.getPage(), request.getNumRows(),
            request.getCodPartida());

        // Calcular offset para paginación
        Integer page = (request.getPage() != null && request.getPage() > 0) ? request.getPage() - 1 : 0;
        Integer numRows = request.getNumRows() != null ? request.getNumRows() : 10;
        int offset = page * numRows;
        int limit = numRows;

        // 1. Primero obtener el total de registros
        Mono<Integer> totalRecordsMono = obtenerTotalRegistros(request.getIdAlmacen(), request.getCodPartida());

        // Determinar qué método usar basado en si hay búsqueda por código de partida
        Flux<AlmacenTachoProjection> dataFlux = obtenerDatosPaginados(request, limit, offset);

        // Consultar en el repository usando la proyección
        return totalRecordsMono
                .flatMap(totalRecords->{
                    log.debug("Total de registros encontrados: {}", totalRecords);

                    // Calcular metadatos de paginación
                    int totalPages = (int) Math.ceil((double) totalRecords / numRows);
                    boolean hasNext = page < (totalPages - 1);
                    boolean hasPrevious = page > 0;
                    log.debug("Metadatos de paginación - Total páginas: {}, Página actual: {}, Tiene siguiente: {}, Tiene anterior: {}", 
            totalPages, page, hasNext, hasPrevious);

                    return dataFlux
                        .map(mapper::toDomain)
                        .doOnNext(resultado -> log.debug("Partida encontrada: ID={}, Código={}", 
                            resultado.getIdPartida(), resultado.getCodPartida())
                        )
                        .collectList()
                        .map(partidas -> AlmacenTachoResponse.builder()
                            .almacenes(partidas)
                            .totalRecords(totalRecords)
                            .totalPages(totalPages)
                            .currentPage(page)
                            .pageSize(numRows)
                            .hasNext(hasNext)
                            .hasPrevious(hasPrevious)
                            .build()
                        );
                })
                .doOnNext(response -> log.info("Consulta completada - Almacén ID: {}, Total: {}, Páginas: {}, Página actual: {}", 
                    request.getIdAlmacen(), response.getTotalRecords(), response.getTotalPages(), response.getCurrentPage()))
                .doOnError(error -> log.error("Error consultando almacén tacho para almacén ID {}: {}", 
                    request.getIdAlmacen(), error.getMessage()));
    }

    /**
     * Consulta partidas con filtros adicionales
     * 
     * @param request Request con criterios de búsqueda
     * @return Flux de partidas filtradas
     */
    public Flux<AlmacenTacho> consultarAlmacenTachoConFiltros(ConsultarAlmacenTachoRequest request) {
        log.debug("Consultando almacén tacho con filtros para almacén ID: {}, codPartida: {}",
                        request.getIdAlmacen(), request.getCodPartida());

        // Calcular offset para paginación
        int offset = request.getPage() * request.getNumRows();
        int limit = request.getNumRows();

        // Usar el método apropiado basado en los filtros disponibles
        Flux<AlmacenTachoProjection> queryResult;
        if (request.getCodPartida() != null && !request.getCodPartida().trim().isEmpty()) {
                log.debug("Ejecutando búsqueda con filtro por código de partida: {}", request.getCodPartida());
                queryResult = repositoryAdapter.findAlmacenTachoByAlmacenIdAndCodPartida(
                                request.getIdAlmacen(),
                                request.getCodPartida().trim(),
                                limit,
                                offset);
        } else {
                log.debug("Ejecutando consulta sin filtros de búsqueda");
                queryResult = repositoryAdapter.findAlmacenTachoByAlmacenId(request.getIdAlmacen(), limit, offset);
        }

        return queryResult
                .map(mapper::toDomain)
                .doOnNext(resultado -> log.debug("Partida filtrada encontrada: ID={}, Código={}",
                                resultado.getIdPartida(), resultado.getCodPartida()))
                .doOnComplete(() -> log.info("Consulta con filtros completada para almacén ID: {}",
                                request.getIdAlmacen()));
    }

        /**
     * Obtiene el total de registros aplicando los mismos filtros que la consulta principal
     */
    private Mono<Integer> obtenerTotalRegistros(Integer idAlmacen, String codPartida) {
        log.debug("Obteniendo total de registros para almacén ID: {}, codPartida: {}", idAlmacen, codPartida);
        
        return repositoryAdapter.countAlmacenTachoByAlmacenId(idAlmacen, codPartida.trim());
    }

    /**
     * Obtiene los datos paginados aplicando los filtros correspondientes
    */
    private Flux<AlmacenTachoProjection> obtenerDatosPaginados(ConsultarAlmacenTachoRequest request, int limit, int offset) {
        log.debug("Obteniendo datos paginados - limit: {}, offset: {}", limit, offset);
        if (request.getCodPartida() != null && !request.getCodPartida().trim().isEmpty()) {
            log.debug("Ejecutando búsqueda con filtro por código de partida: {}", request.getCodPartida());
            return repositoryAdapter.findAlmacenTachoByAlmacenIdAndCodPartida(
                    request.getIdAlmacen(),
                    request.getCodPartida().trim(),
                    limit,
                    offset);
        } else {
            log.debug("Ejecutando consulta sin filtros de búsqueda");
            return repositoryAdapter.findAlmacenTachoByAlmacenId(request.getIdAlmacen(), limit, offset);
        }


    }

}
