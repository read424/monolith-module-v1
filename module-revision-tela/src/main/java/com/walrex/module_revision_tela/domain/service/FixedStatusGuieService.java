package com.walrex.module_revision_tela.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.walrex.module_revision_tela.application.ports.input.FixedStatusGuieUseCase;
import com.walrex.module_revision_tela.application.ports.output.GuiaStatusPort;
import com.walrex.module_revision_tela.application.ports.output.StatusCorreccionPort;
import com.walrex.module_revision_tela.domain.model.GuiaStatusAgrupado;
import com.walrex.module_revision_tela.domain.model.StatusCorreccion;
import com.walrex.module_revision_tela.domain.model.dto.FixedStatusGuieResponse;
import com.walrex.module_revision_tela.domain.model.dto.StatusCorreccionDTO;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.GuiaStatusProjection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa la lógica de negocio para
 * regularizar el status de las guías según los status de los rollos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixedStatusGuieService implements FixedStatusGuieUseCase {

    private final GuiaStatusPort guiaStatusPort;
    private final StatusCorreccionPort statusCorreccionPort;

    @Override
    public Mono<FixedStatusGuieResponse> ejecutarCorreccionStatus() {
        log.info("Iniciando proceso de corrección de status de guías");

        return guiaStatusPort.obtenerGuiasConStatusRollos()
            .collectList()
            .flatMap(this::agruparPorOrdenIngreso)
            .flatMapMany(this::procesarCorrecciones)
            .collectList()
            .map(this::construirRespuesta);
    }

    /**
     * Agrupa las proyecciones por id_ordeningreso y construye el mapa de status
     */
    private Mono<Map<Integer, GuiaStatusAgrupado>> agruparPorOrdenIngreso(List<GuiaStatusProjection> proyecciones) {
        Map<Integer, GuiaStatusAgrupado> guiasAgrupadas = new HashMap<>();

        for (GuiaStatusProjection proyeccion : proyecciones) {
            Integer idOrden = proyeccion.getIdOrdeningreso();

            GuiaStatusAgrupado guia = guiasAgrupadas.computeIfAbsent(idOrden, id -> {
                Map<Integer, Integer> statusMap = new HashMap<>();
                return GuiaStatusAgrupado.builder()
                    .idOrdeningreso(proyeccion.getIdOrdeningreso())
                    .fecIngreso(proyeccion.getFecIngreso())
                    .idCliente(proyeccion.getIdCliente())
                    .idComprobante(proyeccion.getIdComprobante())
                    .nuSerie(proyeccion.getNuSerie())
                    .nuComprobante(proyeccion.getNuComprobante())
                    .statusOrden(proyeccion.getStatusOrden())
                    .statusMap(statusMap)
                    .build();
            });

            // Agregar el status al mapa (key: status calculado, value: status_orden)
            guia.getStatusMap().put(proyeccion.getStatus(), proyeccion.getStatusOrden());
        }

        log.info("Total de guías agrupadas: {}", guiasAgrupadas.size());
        return Mono.just(guiasAgrupadas);
    }

    /**
     * Procesa cada guía y determina si necesita corrección de status
     */
    private Flux<StatusCorreccion> procesarCorrecciones(Map<Integer, GuiaStatusAgrupado> guiasAgrupadas) {
        List<Mono<StatusCorreccion>> correcciones = new ArrayList<>();

        for (GuiaStatusAgrupado guia : guiasAgrupadas.values()) {
            Mono<StatusCorreccion> correccion = determinarCorreccion(guia);
            if (correccion != null) {
                correcciones.add(correccion);
            }
        }

        return Flux.concat(correcciones);
    }

    /**
     * Determina si una guía necesita corrección según la lógica de negocio
     */
    private Mono<StatusCorreccion> determinarCorreccion(GuiaStatusAgrupado guia) {
        Map<Integer, Integer> statusMap = guia.getStatusMap();
        Integer statusOrden = guia.getStatusOrden();
        Integer idOrdeningreso = guia.getIdOrdeningreso();

        // Caso 1: Solo tiene un valor de status
        if (statusMap.size() == 1) {
            Integer statusCalculado = statusMap.keySet().iterator().next();

            // Si son iguales Y ambos son 3, no hacer nada (continue)
            if (statusCalculado.equals(statusOrden) && statusCalculado.equals(3)) {
                log.debug("Guía {} - Status {} ya está correcto (ambos son 3)", idOrdeningreso, statusOrden);
                return Mono.empty();
            }

            // Si son diferentes, registrar la corrección
            log.info("Guía {} - Status actual: {}, Nuevo status: {}",
                idOrdeningreso, statusOrden, statusCalculado);

            return guardarCorreccion(idOrdeningreso, statusOrden, statusCalculado);
        }

        // Caso 2: Tiene más de un valor de status
        if (statusMap.size() > 1) {
            // Si contiene status 1 (en almacén físico)
            if (statusMap.containsKey(1)) {
                log.info("Guía {} - Tiene múltiples status incluyendo 1, nuevo status: 10", idOrdeningreso);
                return guardarCorreccion(idOrdeningreso, statusOrden, 10);
            }

            // Si no contiene 1 pero contiene 10
            if (statusMap.containsKey(10)) {
                log.info("Guía {} - Tiene múltiples status incluyendo 10, nuevo status: 10", idOrdeningreso);
                return guardarCorreccion(idOrdeningreso, statusOrden, 10);
            }

            // Si no contiene ni 1 ni 10, significa que solo tiene status 3
            // En este caso, el nuevo status debe ser 3
            log.info("Guía {} - Solo tiene status 3, nuevo status: 3", idOrdeningreso);
            return guardarCorreccion(idOrdeningreso, statusOrden, 3);
        }

        return Mono.empty();
    }

    /**
     * Guarda la corrección en la base de datos
     */
    private Mono<StatusCorreccion> guardarCorreccion(Integer idOrdeningreso, Integer statusActual, Integer statusNuevo) {
        StatusCorreccion correccion = StatusCorreccion.builder()
            .idOrdeningreso(idOrdeningreso)
            .statusActual(statusActual)
            .statusNuevo(statusNuevo)
            .build();

        return statusCorreccionPort.guardarCorreccion(correccion)
            .doOnSuccess(c -> log.debug("Corrección guardada para guía {}", idOrdeningreso))
            .doOnError(error -> log.error("Error guardando corrección para guía {}: {}",
                idOrdeningreso, error.getMessage()));
    }

    /**
     * Construye la respuesta final del proceso
     */
    private FixedStatusGuieResponse construirRespuesta(List<StatusCorreccion> correcciones) {
        List<StatusCorreccionDTO> correccionesDTO = correcciones.stream()
            .map(c -> new StatusCorreccionDTO(
                c.getIdOrdeningreso(),
                c.getStatusActual(),
                c.getStatusNuevo()
            ))
            .toList();

        String mensaje = correcciones.isEmpty()
            ? "No se encontraron guías que requieran corrección de status"
            : String.format("Se registraron %d correcciones de status", correcciones.size());

        log.info("Proceso de corrección finalizado. Total correcciones: {}", correcciones.size());

        return new FixedStatusGuieResponse(
            correcciones.size(),
            correcciones.size(),
            correccionesDTO,
            mensaje
        );
    }
}
