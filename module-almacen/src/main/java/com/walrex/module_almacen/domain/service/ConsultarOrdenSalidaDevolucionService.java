package com.walrex.module_almacen.domain.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.walrex.module_almacen.application.ports.input.ConsultarOrdenSalidaDevolucionUseCase;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaDevolucionPersistencePort;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper.OrdenSalidaDevolucionResponseMapper;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.ListadoOrdenSalidaDevolucionRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.OrdenSalidaDevolucionResponse;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.PaginatedResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para consultar órdenes de salida por devolución.
 * Implementa paginación y cache para optimizar las consultas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarOrdenSalidaDevolucionService implements ConsultarOrdenSalidaDevolucionUseCase {

    private final OrdenSalidaDevolucionPersistencePort ordenSalidaDevolucionPersistencePort;
    private final OrdenSalidaDevolucionResponseMapper ordenSalidaDevolucionResponseMapper;

    @Override
    @Cacheable(value = "ordenSalidaDevolucion", key = "#request.hashCode()")
    public Mono<PaginatedResponse<OrdenSalidaDevolucionResponse>> consultarOrdenSalidaDevolucion(
            ListadoOrdenSalidaDevolucionRequest request) {
        log.info("📋 Iniciando consulta paginada de órdenes de salida por devolución con cache: {}", request);

        return validarFiltros(request)
                .then(validarPaginacion(request))
                .then(obtenerDatosPaginados(request))
                .doOnNext(response -> log.info("✅ Consulta paginada completada. {} elementos en página {}/{}",
                        response.getPagination().getNumberOfElements(),
                        response.getPagination().getPage() + 1,
                        response.getPagination().getTotalPages()))
                .doOnError(error -> log.error("❌ Error al consultar órdenes de salida por devolución: {}",
                        error.getMessage()));
    }

    /**
     * Obtiene los datos paginados combinando la consulta de datos y el conteo
     * total.
     */
    private Mono<PaginatedResponse<OrdenSalidaDevolucionResponse>> obtenerDatosPaginados(
            ListadoOrdenSalidaDevolucionRequest request) {
        return Mono.zip(
                ordenSalidaDevolucionPersistencePort.obtenerListadoOrdenSalidaDevolucion(request)
                        .map(ordenSalidaDevolucionResponseMapper::toResponse)
                        .collectList(),
                ordenSalidaDevolucionPersistencePort.contarOrdenSalidaDevolucion(request)).map(tuple -> {
                    var ordenes = tuple.getT1();
                    var total = tuple.getT2();

                    var pagination = request.getPagination();
                    var pageMetadata = PaginatedResponse.PageMetadata.calculate(
                            pagination.getPage(),
                            pagination.getSize(),
                            total,
                            ordenes.size(),
                            pagination.getSortBy(),
                            pagination.getSortDirection());

                    return PaginatedResponse.<OrdenSalidaDevolucionResponse>builder()
                            .content(ordenes)
                            .pagination(pageMetadata)
                            .build();
                });
    }

    /**
     * Valida los filtros del request para asegurar coherencia en los datos.
     */
    private Mono<Void> validarFiltros(ListadoOrdenSalidaDevolucionRequest request) {
        return Mono.fromRunnable(() -> {
            // Validar que fechaInicio no sea posterior a fechaFin
            if (request.getFechaInicio() != null && request.getFechaFin() != null) {
                if (request.getFechaInicio().isAfter(request.getFechaFin())) {
                    throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
                }
            }

            log.debug("✅ Validación de filtros completada exitosamente");
        });
    }

    /**
     * Valida los parámetros de paginación.
     */
    private Mono<Void> validarPaginacion(ListadoOrdenSalidaDevolucionRequest request) {
        return Mono.fromRunnable(() -> {
            var pagination = request.getPagination();

            if (!pagination.isValid()) {
                throw new IllegalArgumentException(
                        "Parámetros de paginación inválidos: página >= 0, tamaño entre 1 y 100");
            }

            // Validar campos de ordenamiento permitidos
            var allowedSortFields = java.util.Set.of("id_ordensalida", "cod_salida", "fec_entrega");
            if (!allowedSortFields.contains(pagination.getSortBy())) {
                throw new IllegalArgumentException("Campo de ordenamiento no permitido: " + pagination.getSortBy());
            }

            // Validar dirección de ordenamiento
            var allowedSortDirections = java.util.Set.of("ASC", "DESC");
            if (!allowedSortDirections.contains(pagination.getSortDirection().toUpperCase())) {
                throw new IllegalArgumentException(
                        "Dirección de ordenamiento no permitida: " + pagination.getSortDirection());
            }

            log.debug("✅ Validación de paginación completada exitosamente");
        });
    }
}