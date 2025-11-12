package com.walrex.module_comercial.domain.service;

import com.walrex.module_comercial.application.ports.input.GetOrdenProduccionPartidaUseCase;
import com.walrex.module_comercial.domain.dto.OrdenProductionPartidaResponseDTO;
import com.walrex.module_comercial.domain.dto.ProcesosProduccionDTO;
import com.walrex.module_comercial.domain.exceptions.PartidaDesactivadaException;
import com.walrex.module_comercial.domain.exceptions.PartidaNoHabilitadaException;
import com.walrex.module_comercial.domain.exceptions.PartidaParcialmenteDespachadaException;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.ComercialRepositoryAdapter;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.dto.ProcesosPartidaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdenProduccionAssignmentService implements GetOrdenProduccionPartidaUseCase {
    private final ComercialRepositoryAdapter comercialRepositoryAdapter;

    @Override
    public Mono<OrdenProductionPartidaResponseDTO> getInfoOrdenProduccionPartida(Integer idPartida) {
        log.debug("Iniciando consulta de información de orden de producción para partida: {}", idPartida);

        return comercialRepositoryAdapter.getInfoOrdenProduccionPartida(idPartida)
            .flatMap(ordenPartida -> {
                log.debug("Información de partida obtenida: {}", ordenPartida);

                // Validación 1: Verificar si la partida está desactivada (condition == 0)
                if (ordenPartida.getCondition() != null && ordenPartida.getCondition() == 0) {
                    log.error("La partida {} está desactivada", idPartida);
                    return Mono.error(new PartidaDesactivadaException(idPartida));
                }

                // Validación 2: Verificar si el status está en los valores permitidos [0, 1, 2]
                Integer status = ordenPartida.getStatus();
                if (status == null || (status != 0 && status != 1 && status != 2)) {
                    log.error("La partida {} tiene un status no habilitado: {}", idPartida, status);
                    return Mono.error(new PartidaNoHabilitadaException(idPartida, status));
                }

                log.debug("Validaciones de partida exitosas, consultando procesos...");

                // Construir el builder inicial con los datos de la partida (SIN llamar a build() todavía)
                OrdenProductionPartidaResponseDTO.OrdenProductionPartidaResponseDTOBuilder builder =
                    OrdenProductionPartidaResponseDTO.builder()
                        .idOrdenProduccion(ordenPartida.getIdOrdenProduccion())
                        .codOrdenProduccion(ordenPartida.getCodOrdenProduccion())
                        .descArticulo(ordenPartida.getDescArticulo())
                        .idOrden(ordenPartida.getIdOrden())
                        .precio(ordenPartida.getPrecio())
                        .idDetOS(ordenPartida.getIdDetOs())
                        .idOrdenIngreso(ordenPartida.getIdOrdenIngreso())
                        .cntPartidas(0); // No debo considerar la partida actual

                // Paso 1: Obtener procesos y setear valores relacionados
                return comercialRepositoryAdapter.getProcesosPartidaStatus(idPartida)
                    .collectList()
                    .doOnNext(procesos -> log.debug("Procesos obtenidos: {}", procesos.size()))
                    .flatMap(procesos -> {
                        // Evaluar isExecute: si algún proceso tiene isMainProceso == 1 y fecRealInicio != null
                        boolean isExecute = procesos.stream()
                            .anyMatch(proceso ->
                                proceso.getIsMainProceso() != null &&
                                proceso.getIsMainProceso() == 1 &&
                                proceso.getFecRealInicio() != null
                            );

                        log.debug("¿Proceso principal iniciado?: {}", isExecute);

                        // Mapear procesos de infrastructure a domain DTOs
                        ProcesosProduccionDTO[] procesosArray = procesos.stream()
                            .map(this::mapToProcesoProduccionDTO)
                            .toArray(ProcesosProduccionDTO[]::new);

                        // Setear valores relacionados a procesos en el builder
                        builder.idRuta(procesos.isEmpty() ? null : procesos.get(0).getIdRuta())
                               .isMainProductionSuccess(isExecute)
                               .procesos(procesosArray);

                        // Paso 2: Obtener status de despacho y validar partida actual
                        return comercialRepositoryAdapter.getStatusDespachoPartidas(ordenPartida.getIdOrdenProduccion())
                            .collectList()
                            .flatMap(despachos -> {
                                log.debug("Despachos obtenidos: {}", despachos.size());

                                // Validación: Si la partida actual está parcialmente despachada, lanzar excepción
                                boolean partidaParcialmenteDespachada = despachos.stream()
                                    .anyMatch(despacho ->
                                        despacho.getIdPartida() != null &&
                                        despacho.getIdPartida().equals(idPartida) &&
                                        despacho.getCntDespachado() != null &&
                                        despacho.getCntDespachado() > 0
                                    );

                                if (partidaParcialmenteDespachada) {
                                    log.error("La partida {} está parcialmente despachada", idPartida);
                                    return Mono.error(new PartidaParcialmenteDespachadaException(idPartida));
                                }

                                // Contar partidas sin considerar la partida actual
                                long cntPartidas = despachos.stream()
                                    .filter(despacho -> despacho.getIdPartida() != null && !despacho.getIdPartida().equals(idPartida))
                                    .count();

                                log.debug("Cantidad de partidas (sin considerar la actual): {}", cntPartidas);

                                // Verificar si alguna partida (excluyendo la actual) está despachada
                                boolean isDelivered = despachos.stream()
                                    .anyMatch(despacho ->
                                        despacho.getIdPartida() != null &&
                                        !despacho.getIdPartida().equals(idPartida) &&
                                        despacho.getCntDespachado() != null &&
                                        despacho.getCntDespachado() > 0
                                    );

                                log.debug("¿Alguna partida despachada?: {}", isDelivered);

                                // Setear valores finales y construir el objeto
                                builder.cntPartidas((int) cntPartidas)
                                       .isDelivered(isDelivered ? 1 : 0);

                                log.info("Información de orden de producción construida exitosamente para partida: {}", idPartida);

                                return Mono.just(builder.build());
                            });
                    });
            })
            .doOnError(error -> log.error("Error procesando información de orden de producción para partida {}: {}",
                idPartida, error.getMessage()));
    }

    /**
     * Mapea ProcesosPartidaDTO (infrastructure) a ProcesosProduccionDTO (domain)
     */
    private ProcesosProduccionDTO mapToProcesoProduccionDTO(ProcesosPartidaDTO procesoPartida) {
        return ProcesosProduccionDTO.builder()
            .idPartidaMaquina(procesoPartida.getIdPartidaMaquina())
            .idProceso(procesoPartida.getIdProceso())
            .noProceso(procesoPartida.getNoProceso())
            .isMainProceso(procesoPartida.getIsMainProceso())
            .isStarted(procesoPartida.getFecRealInicio() != null ? 1 : 0)
            .isFinish(procesoPartida.getFecRealFin() != null ? 1 : 0)
            .status(procesoPartida.getStatus())
            .build();
    }
}
