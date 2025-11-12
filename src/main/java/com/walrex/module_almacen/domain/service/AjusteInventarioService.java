package com.walrex.module_almacen.domain.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.walrex.module_almacen.application.ports.input.ProcesarAjusteInventarioUseCase;
import com.walrex.module_almacen.application.ports.output.RegistrarEgresoPort;
import com.walrex.module_almacen.application.ports.output.RegistrarIngresoPort;
import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.domain.model.mapper.ResultadoAjusteMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
@Slf4j
public class AjusteInventarioService implements ProcesarAjusteInventarioUseCase {
    private final RegistrarIngresoPort registrarIngresoPort;
    private final RegistrarEgresoPort registrarEgresoPort;
    private final ReactiveTransactionManager transactionManager;
    private final ResultadoAjusteMapper resultadoAjusteMapper;

    @Override
    @Transactional
    public Mono<ResponseAjusteInventoryDTO> procesarAjusteInventario(
            RequestAjusteInventoryDTO request, String correlationId) {

        MDC.put("correlationId", correlationId);
        log.info("Iniciando procesamiento de ajuste de inventario [correlationId={}]", correlationId);

        if (log.isDebugEnabled()) {
            log.debug("Detalles de la solicitud de ajuste: almacén={}, motivo={}, fecha={}",
                    request.getId_almacen(), request.getId_motivo(), request.getFec_actualizacion());
        }

        // Crear el operador transaccional
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        int num_ingresos = request.getIngresos().size();
        int num_egresos = request.getEgresos().size();

        log.info("Ajuste con {} ingresos y {} egresos [correlationId={}]",
                num_ingresos, num_egresos, correlationId);

        if (num_ingresos == 0 && num_egresos == 0) {
            log.warn("Ajuste de inventario sin ingresos ni egresos. TransactionId: {}", correlationId);
            MDC.remove("correlationId");
            return Mono.just(ResponseAjusteInventoryDTO.builder()
                    .isSuccess(false)
                    .message("El ajuste no contiene ingresos ni egresos")
                    .transactionId(correlationId)
                    .build());
        }

        return procesarAjusteCompleto(request, correlationId)
                .as(transactionalOperator::transactional)
                .doOnSubscribe(
                        s -> log.debug("Iniciando transacción para procesar ajuste [correlationId={}]", correlationId))
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        log.info("Ajuste de inventario procesado correctamente [correlationId={}]", correlationId);
                    } else {
                        log.warn("Ajuste de inventario no exitoso: {} [correlationId={}]",
                                response.getMessage(), correlationId);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error durante la transacción para TransactionId: {}. Error: {}",
                            correlationId, error.getMessage(), error);

                    return Mono.just(ResponseAjusteInventoryDTO.builder()
                            .isSuccess(false)
                            .message("Error al procesar ajuste de inventario: " + error.getMessage())
                            .transactionId(correlationId)
                            .build());
                })
                .doFinally(signal -> MDC.remove("correlationId"));
    }

    private Mono<ResponseAjusteInventoryDTO> procesarAjusteCompleto(RequestAjusteInventoryDTO ajuste,
            String transactionId) {
        log.debug("Procesando ajuste completo. TransactionId: {}, AlmacenId: {}, MotivoId: {}",
                transactionId, ajuste.getId_almacen(), ajuste.getId_motivo());

        return procesarIngresos(ajuste, transactionId)
                .doOnSuccess(ingresoResult -> {
                    int size = ingresoResult.getDetalles() != null ? ingresoResult.getDetalles().size() : 0;
                    log.debug("Procesados {} ingresos. TransactionId: {}", size, transactionId);
                })
                .flatMap(ingresoResult -> procesarEgresos(ajuste, transactionId)
                        .doOnSuccess(egresoResult -> {
                            int size = egresoResult.getDetalles() != null ? egresoResult.getDetalles().size() : 0;
                            log.debug("Procesados {} egresos. TransactionId: {}", size, transactionId);
                        })
                        .map(egresoResult -> Tuples.of(ingresoResult, egresoResult)))
                .map(tuple -> {
                    OrdenIngresoDTO ingresoResult = tuple.getT1();
                    OrdenEgresoDTO egresoResult = tuple.getT2();

                    // String idReferencia = construirIdReferencia(ingresoResult, egresoResult);
                    int ingresosCount = ingresoResult.getDetalles().size();
                    int egresosCount = egresoResult.getDetalles().size();

                    log.info("Ajuste de inventario procesado correctamente. TransactionId: {}, " +
                            "Ingresos: {}, Egresos: {}", transactionId, ingresosCount, egresosCount);

                    // Construir los objetos de resultado para ingresos y egresos
                    ResultAjustIngresoDTO resultIngresos = construirResultadoIngresos(ingresoResult);
                    ResultAjustEgresoDTO resultEgresos = construirResultadoEgresos(egresoResult);

                    return ResponseAjusteInventoryDTO.builder()
                            .isSuccess(true)
                            .message("Ajuste de inventario procesado correctamente")
                            .transactionId(transactionId)
                            .result_ingresos(resultIngresos)
                            .result_egresos(resultEgresos)
                            .build();
                });
    }

    private Mono<OrdenIngresoDTO> procesarIngresos(RequestAjusteInventoryDTO ingreso, String transactionId) {
        if (ingreso.getIngresos() == null || ingreso.getIngresos().isEmpty()) {
            log.debug("No hay ingresos para procesar. TransactionId: {}", transactionId);
            return Mono.just(OrdenIngresoDTO.builder()
                    .detalles(Collections.emptyList())
                    .build());
        }
        log.debug("Procesando {} ingresos para TransactionId: {}",
                ingreso.getIngresos().size(), transactionId);
        return registrarIngresoPort.registrarIngreso(
                ingreso.getId_motivo(),
                ingreso.getId_almacen(),
                ingreso.getFec_actualizacion(),
                "AJUSTE DE INVENTARIO",
                ingreso.getIngresos(),
                transactionId).doOnSuccess(result -> {
                    log.debug("Ingresos procesados exitosamente. OrdenId: {}, TransactionId: {}",
                            result.getId(), transactionId);
                })
                .doOnError(error -> {
                    log.error("Error al procesar ingresos. TransactionId: {}, Error: {}",
                            transactionId, error.getMessage(), error);
                });
    }

    private Mono<OrdenEgresoDTO> procesarEgresos(RequestAjusteInventoryDTO egreso, String transactionId) {
        if (egreso.getEgresos() == null || egreso.getEgresos().isEmpty()) {
            log.debug("No hay egresos para procesar. TransactionId: {}", transactionId);
            return Mono.just(OrdenEgresoDTO.builder()
                    .detalles(Collections.emptyList())
                    .build());
        }
        log.debug("Procesando {} egresos para TransactionId: {}", egreso.getEgresos().size(), transactionId);

        return registrarEgresoPort.registrarEgreso(
                egreso.getId_motivo(),
                egreso.getId_almacen(),
                egreso.getFec_actualizacion(),
                "AJUSTE DE INVENTARIO",
                egreso.getEgresos(),
                transactionId).doOnSuccess(result -> {
                    log.debug("Egresos procesados exitosamente. OrdenId: {}, TransactionId: {}",
                            result.getId(), transactionId);
                })
                .doOnError(error -> {
                    log.error("Error al procesar egresos. TransactionId: {}, Error: {}",
                            transactionId, error.getMessage(), error);
                });
    }

    private ResultAjustIngresoDTO construirResultadoIngresos(OrdenIngresoDTO ingresoResult) {
        if (ingresoResult == null || ingresoResult.getId() == null) {
            return null;
        }
        List<ItemResultSavedDTO> detalles = ingresoResult.getDetalles() != null ? ingresoResult.getDetalles().stream()
                .map(resultadoAjusteMapper::detalleIngresoToItemResult)
                .collect(Collectors.toList()) : Collections.emptyList();

        return ResultAjustIngresoDTO.builder()
                .id(ingresoResult.getId().intValue())
                .num_saved(detalles.size())
                .details(detalles)
                .build();
    }

    private ResultAjustEgresoDTO construirResultadoEgresos(OrdenEgresoDTO egresoResult) {
        if (egresoResult == null || egresoResult.getId() == null) {
            return null;
        }

        List<ItemArticuloEgreso> detalles = egresoResult.getDetalles() != null ? egresoResult.getDetalles().stream()
                .map(resultadoAjusteMapper::detalleSalidaToItemResult)
                .collect(Collectors.toList()) : Collections.emptyList();

        return ResultAjustEgresoDTO.builder()
                .id(egresoResult.getId().intValue())
                .num_saved(detalles.size())
                .details(detalles)
                .build();
    }

    /*
     * private String construirIdReferencia(OrdenIngresoDTO ingreso, OrdenEgresoDTO
     * egreso) {
     * StringBuilder ref = new StringBuilder();
     * if (ingreso != null && ingreso.getId() != null) {
     * ref.append("I").append(ingreso.getId());
     * }
     * if (egreso != null && egreso.getId() != null) {
     * if (ref.length() > 0) {
     * ref.append("-");
     * }
     * ref.append("E").append(egreso.getId());
     * }
     * 
     * return ref.length() > 0 ? ref.toString() : null;
     * }
     */
}
