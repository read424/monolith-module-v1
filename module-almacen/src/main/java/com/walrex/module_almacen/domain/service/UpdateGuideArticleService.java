package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.UpdateGuideArticleUseCase;
import com.walrex.module_almacen.application.ports.output.UpdateGuideArticleOutputPort;
import com.walrex.module_almacen.domain.model.dto.UpdateGuideArticleRequest;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleConflictException;
import com.walrex.module_almacen.domain.model.exceptions.GuideArticleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateGuideArticleService implements UpdateGuideArticleUseCase {

    private final UpdateGuideArticleOutputPort outputPort;

    @Override
    @Transactional
    public Mono<Void> updateGuideArticle(Integer idDetalleOrden, UpdateGuideArticleRequest request) {
        log.info("Actualizando artículo de guía id_detordeningreso={} con articulo={}, peso_ref={}, nu_rollos={}",
                idDetalleOrden, request.getId_articulo(), request.getPeso_ref(), request.getNu_rollos());

        return outputPort.findIdOrdenIngresoByIdDetalleOrden(idDetalleOrden)
                .switchIfEmpty(Mono.error(new GuideArticleNotFoundException(
                        "No se encontró el detalle de guía con id_detordeningreso=" + idDetalleOrden)))
                .flatMap(idOrdenIngreso -> outputPort.countExistingRolls(idDetalleOrden)
                        .flatMap(existingRolls -> validateRollCount(idDetalleOrden, request, existingRolls))
                        .then(Mono.defer(() -> outputPort.existsProductionOrderByIdOrdenIngreso(idOrdenIngreso)))
                        .flatMap(existsProductionOrder -> validateProductionOrder(idOrdenIngreso, existsProductionOrder))
                        .then(Mono.defer(() -> outputPort.existsAssignedPartidaByIdDetalleOrden(idDetalleOrden)))
                        .flatMap(existsAssignedPartida -> validateAssignedPartida(idDetalleOrden, existsAssignedPartida))
                        .then(Mono.defer(() -> outputPort.updateGuideArticle(idDetalleOrden, request))));
    }

    private Mono<Void> validateRollCount(
            Integer idDetalleOrden, UpdateGuideArticleRequest request, Long existingRolls) {
        long requestedRolls = request.getNu_rollos();
        if (requestedRolls < existingRolls) {
            return Mono.error(new GuideArticleConflictException(
                    "La nueva cantidad de rollos no puede ser menor a los rollos existentes para id_detordeningreso="
                            + idDetalleOrden + ". existentes=" + existingRolls + ", solicitados=" + requestedRolls));
        }
        return Mono.empty();
    }

    private Mono<Void> validateProductionOrder(Long idOrdenIngreso, Boolean existsProductionOrder) {
        if (Boolean.TRUE.equals(existsProductionOrder)) {
            return Mono.error(new GuideArticleConflictException(
                    "No se puede actualizar el detalle porque la orden de ingreso " + idOrdenIngreso
                            + " ya tiene una orden de producción asociada"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateAssignedPartida(Integer idDetalleOrden, Boolean existsAssignedPartida) {
        if (Boolean.TRUE.equals(existsAssignedPartida)) {
            return Mono.error(new GuideArticleConflictException(
                    "No se puede actualizar el detalle de guía porque existen rollos asignados a una partida para id_detordeningreso="
                            + idDetalleOrden));
        }
        return Mono.empty();
    }
}
