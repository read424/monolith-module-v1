package com.walrex.module_revision_tela.domain.service;

import com.walrex.module_revision_tela.application.ports.input.ReviewInventoryAndLiftingUseCase;
import com.walrex.module_revision_tela.application.ports.output.AnalysisLiftingRevisionPort;
import com.walrex.module_revision_tela.domain.exceptions.InsufficientRollosException;
import com.walrex.module_revision_tela.domain.model.dto.AnalysisInventoryLiftingResponse;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowInventoryLiftingRoll;
import com.walrex.module_revision_tela.infrastructure.adapters.outbound.persistence.projection.RowLevantamientoProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiftingInventoryService implements ReviewInventoryAndLiftingUseCase {

    private final AnalysisLiftingRevisionPort analysisLiftingRevisionPort;

    /**
     * Clase interna para acumular estadísticas durante el procesamiento
     */
    private static class ProcessingStats {
        int totalLevantamientos = 0;
        int totalRollosAsignados = 0;

        void addLevantamiento(int rollosAsignados) {
            this.totalLevantamientos++;
            this.totalRollosAsignados += rollosAsignados;
        }
    }

    /**
     * Ejecuta el análisis de inventario levantado para un periodo específico
     *
     * @Transactional garantiza que todas las actualizaciones sean atómicas:
     * - Si falla un UPDATE, se hace rollback de todos los cambios
     * - Si se lanza InsufficientRollosException, ningún rollo quedará asignado
     * - Evita inconsistencias en la asignación de id_levantamiento
     *
     * @return Mono con las estadísticas del procesamiento
     */
    @Transactional
    public Mono<AnalysisInventoryLiftingResponse> executeAnalysis(Integer idPeriodo){
        log.info("Iniciando análisis transaccional de inventario levantado para periodo: {}", idPeriodo);

        // Crear Map estructurado para el inventario levantado
        // Estructura: Map<idDetOrdenIngreso, Map<idPartida, List<RowLevantamientoProjection>>>
        Mono<Map<Integer, Map<Integer, List<RowLevantamientoProjection>>>> levantamientoData =
            analysisLiftingRevisionPort.getLiftingInventory(idPeriodo)
                .doOnNext(row -> log.debug("Levantamiento obtenido: idDetOrden={}, idPartida={}, cntRolls={}",
                    row.getIdDetOrdenIngreso(), row.getIdPartida(), row.getCntRolls()))
                .doOnError(error -> log.error("[executeAnalysis] Error en getLiftingInventory para periodo {}: {}",
                    idPeriodo, error.getMessage(), error))
                .groupBy(RowLevantamientoProjection::getIdDetOrdenIngreso)
                .flatMap(group1 ->
                    group1.groupBy(RowLevantamientoProjection::getIdPartida)
                        .flatMap(group2 ->
                            group2.collectList()
                                .map(list -> Map.entry(group2.key(), list))
                        )
                        .collectList()
                        .map(entries -> {
                            // Convertir a TreeMap con orden reverso (partidas específicas primero)
                            Map<Integer, List<RowLevantamientoProjection>> innerMap = new TreeMap<>(Comparator.reverseOrder());
                            entries.forEach(entry -> innerMap.put(entry.getKey(), entry.getValue()));
                            return Map.entry(group1.key(), innerMap);
                        })
                )
                .collectList()
                .map(entries -> {
                    Map<Integer, Map<Integer, List<RowLevantamientoProjection>>> resultMap = new HashMap<>();
                    entries.forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));
                    return resultMap;
                })
                .doOnSuccess(map ->
                    log.info("Map de levantamiento creado con {} detOrdenIngreso", map.size())
                )
                .doOnError(error -> log.error("[executeAnalysis] Error al construir mapa de levantamiento: {}",
                    error.getMessage(), error));

        // Ejecutar el matching y asignación de rollos, acumulando estadísticas
        return matchLiftingAndRevision(idPeriodo, levantamientoData)
            .map(stats -> {
                log.info("Análisis completado exitosamente para periodo: {} - {} levantamientos, {} rollos asignados",
                    idPeriodo, stats.totalLevantamientos, stats.totalRollosAsignados);

                return new AnalysisInventoryLiftingResponse(
                    idPeriodo,
                    stats.totalLevantamientos,
                    stats.totalRollosAsignados,
                    String.format("Proceso completado: %d levantamientos procesados, %d rollos asignados",
                        stats.totalLevantamientos, stats.totalRollosAsignados)
                );
            })
            .onErrorResume(error -> {
                log.error("[executeAnalysis] Error fatal durante análisis para periodo {}: {} - Clase: {}",
                    idPeriodo, error.getMessage(), error.getClass().getSimpleName(), error);
                return Mono.error(new RuntimeException(
                    String.format("[executeAnalysis] Error en periodo %d: %s", idPeriodo, error.getMessage()), error));
            });
    }

    //recibo el periodo y la data de registros de levantamiento
    private Mono<ProcessingStats> matchLiftingAndRevision(Integer idPeriodo, Mono<Map<Integer, Map<Integer, List<RowLevantamientoProjection>>>> data){
        ProcessingStats stats = new ProcessingStats();

        return data
            .doOnError(error -> log.error("[matchLiftingAndRevision] Error al obtener data de levantamiento: {}",
                error.getMessage(), error))
            .flatMapMany(map -> {
                log.info("[matchLiftingAndRevision] Procesando {} entradas de detOrdenIngreso", map.size());
                // Usar concatMap para procesar secuencialmente y evitar saturar conexiones R2DBC
                return Flux.fromIterable(map.entrySet())
                    .concatMap(entry -> {
                        Integer idDetOrdenIngreso = entry.getKey();
                        Map<Integer, List<RowLevantamientoProjection>> levantamientoMap = entry.getValue();

                        log.info("[matchLiftingAndRevision] Procesando idDetOrdenIngreso: {}", idDetOrdenIngreso);

                        // Obtener todos los rollos de observación inventariados para este detOrdenIngreso
                        return analysisLiftingRevisionPort.getRollosObservationInventory(
                            idPeriodo,
                            idDetOrdenIngreso
                        )
                            .doOnSubscribe(s -> log.debug("[matchLiftingAndRevision] Consultando rollos para periodo={}, idDetOrdenIngreso={}",
                                idPeriodo, idDetOrdenIngreso))
                            .doOnError(error -> log.error("[matchLiftingAndRevision] Error en getRollosObservationInventory " +
                                    "para periodo={}, idDetOrdenIngreso={}: {}",
                                idPeriodo, idDetOrdenIngreso, error.getMessage(), error))
                            .collectList()
                            .map(rollosList -> {
                                // Agrupar rollos por partida en memoria
                                Map<Integer, List<RowInventoryLiftingRoll>> rollosMap = new HashMap<>();
                                for (RowInventoryLiftingRoll rollo : rollosList) {
                                    rollosMap.computeIfAbsent(rollo.getId_partida(), k -> new ArrayList<>()).add(rollo);
                                }
                                return rollosMap;
                            })
                            .doOnSuccess(rollosMap -> log.debug("[matchLiftingAndRevision] Rollos agrupados exitosamente, partidas: {}",
                                rollosMap.keySet()))
                            .flatMapMany(rollosObservacionMap -> {
                                log.info("[matchLiftingAndRevision] Rollos de observación agrupados por partida: {}",
                                    rollosObservacionMap.keySet());

                                // Procesar cada entrada de levantamiento secuencialmente
                                return Flux.fromIterable(levantamientoMap.entrySet())
                                    .concatMap(levEntry -> {
                                        Integer idPartida = levEntry.getKey();
                                        // Get List data Levantamiento
                                        List<RowLevantamientoProjection> levantamientoList = levEntry.getValue();

                                        log.info("[matchLiftingAndRevision] Procesando partida: {}, con {} registros de levantamiento",
                                            idPartida, levantamientoList.size());

                                        // Procesar cada registro de levantamiento secuencialmente
                                        return Flux.fromIterable(levantamientoList)
                                            .concatMap(levantamiento ->
                                                processLevantamiento(
                                                    levantamiento,
                                                    rollosObservacionMap,
                                                    idPartida
                                                )
                                                .doOnSuccess(rollosAsignados -> stats.addLevantamiento(rollosAsignados))
                                                .doOnError(error -> log.error("[matchLiftingAndRevision] Error en processLevantamiento " +
                                                        "para idLevantamiento={}, partida={}: {}",
                                                    levantamiento.getIdLevantamiento(), idPartida, error.getMessage(), error))
                                            );
                                    });
                            })
                            .onErrorResume(error -> {
                                log.error("[matchLiftingAndRevision] Error procesando idDetOrdenIngreso={}: {}",
                                    idDetOrdenIngreso, error.getMessage(), error);
                                return Flux.error(new RuntimeException(
                                    String.format("[matchLiftingAndRevision] Error en idDetOrdenIngreso %d: %s",
                                        idDetOrdenIngreso, error.getMessage()), error));
                            });
                    });
            })
        .then(Mono.just(stats));
    }

    /**
     * Procesa un registro de levantamiento individual y asigna rollos
     *
     * @return Mono<Integer> con la cantidad de rollos asignados
     */
    private Mono<Integer> processLevantamiento(
        RowLevantamientoProjection levantamiento,
        Map<Integer, List<RowInventoryLiftingRoll>> rollosObservacionMap,
        Integer idPartida
    ) {
        Integer cntRollosRequeridos = levantamiento.getCntRolls();//Rollos levantados
        Integer idLevantamiento = levantamiento.getIdLevantamiento();//id levantamiento a setear

        log.info("Asignando {} rollos al levantamiento id: {}, partida: {}",
            cntRollosRequeridos, idLevantamiento, idPartida);

        // Obtener los rollos disponibles según la estrategia de partidas
        List<RowInventoryLiftingRoll> rollosDisponibles = getRollosDisponiblesStrategy(
            rollosObservacionMap,
            idPartida
        );

        if (rollosDisponibles == null || rollosDisponibles.isEmpty()) {
            log.warn("No hay rollos disponibles para levantamiento id: {}, partida: {}",
                idLevantamiento, idPartida);
            return Mono.just(0);
        }

        // Validación: Filtrar solo rollos que NO tengan id_levantamiento asignado
        rollosDisponibles = rollosDisponibles.stream()
            .filter(rollo -> rollo.getId_levantamiento() == null)
            .toList();

        if (rollosDisponibles.isEmpty()) {
            log.warn("Todos los rollos ya tienen id_levantamiento asignado para partida: {}", idPartida);
            return Mono.just(0);
        }

        log.debug("Rollos disponibles sin id_levantamiento: {}", rollosDisponibles.size());

        // Validar que haya suficientes rollos disponibles
        if (rollosDisponibles.size() < cntRollosRequeridos) {
            log.error("Rollos insuficientes para levantamiento id: {} - Requeridos: {}, Disponibles: {}",
                idLevantamiento, cntRollosRequeridos, rollosDisponibles.size());
            throw new InsufficientRollosException(
                idLevantamiento,
                cntRollosRequeridos,
                rollosDisponibles.size()
            );
        }

        // Tomar solo la cantidad necesaria de rollos y setear el idLevantamiento
        List<RowInventoryLiftingRoll> rollosSeleccionados = rollosDisponibles.stream()
            .limit(cntRollosRequeridos)
            .peek(rollo -> rollo.setId_levantamiento(idLevantamiento))
            .toList();

        // Extraer IDs (PK de detail_rollo_revision) para la actualización
        List<Integer> idsRollos = rollosSeleccionados.stream()
            .map(RowInventoryLiftingRoll::getId)
            .toList();

        log.debug("Actualizando {} rollos (IDs: {}) con idLevantamiento={}",
            idsRollos.size(), idsRollos, idLevantamiento);

        // Remover los rollos asignados del pool disponible
        //rollosDisponibles.subList(0, Math.min(cntRollosRequeridos, rollosDisponibles.size())).clear();

        // Actualizar los rollos con el id_levantamiento en BD
        // y luego decrementar la cantidad_disponible del levantamiento
        return analysisLiftingRevisionPort.updateRollosWithLevantamientoId(
            idsRollos,
            idLevantamiento
        )
            .doOnSuccess(updated ->
                log.info("Actualizados {} rollos con levantamiento id: {}", updated, idLevantamiento)
            )
            .flatMap(updatedCount -> {
                if (updatedCount > 0) {
                    // Decrementar cantidad_disponible por la cantidad de rollos asignados
                    return analysisLiftingRevisionPort.decrementarCantidadDisponible(idLevantamiento, updatedCount)
                        .doOnSuccess(decremented ->
                            log.info("Decrementada cantidad_disponible en {} para levantamiento id: {}",
                                updatedCount, idLevantamiento)
                        )
                        .thenReturn(updatedCount);
                }
                return Mono.just(updatedCount);
            });
    }

    /**
     * Estrategia para obtener rollos disponibles según la lógica de partidas:
     * 1. Si idPartida != 0, intentar usar rollos de esa partida específica
     * 2. Si no hay rollos suficientes o idPartida == 0, usar rollos sin partida (id_partida = 0)
     */
    private List<RowInventoryLiftingRoll> getRollosDisponiblesStrategy(
        Map<Integer, List<RowInventoryLiftingRoll>> rollosObservacionMap,
        Integer idPartida
    ) {
        if (idPartida != null && idPartida != 0) {
            // Intentar obtener rollos de la partida específica
            List<RowInventoryLiftingRoll> rollosPartida = rollosObservacionMap.get(idPartida);

            if (rollosPartida != null && !rollosPartida.isEmpty()) {
                log.debug("Usando rollos de partida específica: {}", idPartida);
                return rollosPartida;
            } else {
                log.debug("No hay rollos de partida {}, usando rollos sin partida (0)", idPartida);
            }
        }

        // Fallback: usar rollos sin partida asignada (id_partida = 0)
        return rollosObservacionMap.getOrDefault(0, new ArrayList<>());
    }

}
