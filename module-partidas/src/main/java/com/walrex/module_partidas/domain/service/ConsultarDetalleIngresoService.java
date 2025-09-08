package com.walrex.module_partidas.domain.service;

import org.springframework.stereotype.Service;

import com.walrex.module_partidas.application.ports.input.ConsultarDetalleIngresoUseCase;
import com.walrex.module_partidas.application.ports.output.ConsultarDetalleIngresoPort;
import com.walrex.module_partidas.domain.model.DetalleIngresoRollos;
import com.walrex.module_partidas.domain.model.dto.ConsultarDetalleIngresoRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso para consultar detalle de ingreso con rollos
 * Delega la consulta al puerto de salida correspondiente
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarDetalleIngresoService implements ConsultarDetalleIngresoUseCase {

    private final ConsultarDetalleIngresoPort consultarDetalleIngresoPort;

    @Override
    public Mono<DetalleIngresoRollos> consultarDetalleIngreso(ConsultarDetalleIngresoRequest request) {
        log.info("Ejecutando consulta de detalle de ingreso para partida ID: {} en almacén ID: {}",
                request.getIdPartida(), request.getIdAlmacen());

        return consultarDetalleIngresoPort.consultarDetalleIngreso(request)
                .doOnSuccess(resultado -> {
                    if (resultado != null) {
                        log.debug("Detalle de ingreso procesado: ID={}, Rollos={}",
                                resultado.getIdDetordeningreso(), resultado.getRollos().size());
                    }
                })
                .doOnSuccess(resultado -> log.info(
                        "Consulta de detalle de ingreso completada exitosamente para partida ID: {} en almacén ID: {}",
                        request.getIdPartida(), request.getIdAlmacen()))
                .doOnError(error -> log.error(
                        "Error en consulta de detalle de ingreso para partida ID {} en almacén ID {}: {}",
                        request.getIdPartida(), request.getIdAlmacen(), error.getMessage()));
    }
}
