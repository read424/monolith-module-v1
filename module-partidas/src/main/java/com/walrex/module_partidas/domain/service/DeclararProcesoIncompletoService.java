package com.walrex.module_partidas.domain.service;

import com.walrex.module_partidas.application.ports.input.DeclararProcesoIncompletoUseCase;
import com.walrex.module_partidas.application.ports.output.AlmacenPort;
import com.walrex.module_partidas.application.ports.output.ProcesarDeclaracionesPort;
import com.walrex.module_partidas.domain.model.dto.DetailProcesoProductionDTO;
import com.walrex.module_partidas.domain.model.dto.ItemProcessProductionDTO;
import com.walrex.module_partidas.domain.model.dto.ProcesoDeclararItemDTO;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.PartidaInfoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.RollsInStoreProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para declarar procesos incompletos.
 * Implementa el caso de uso y orquesta la lógica de negocio.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeclararProcesoIncompletoService implements DeclararProcesoIncompletoUseCase {

    private static final Integer ALMACEN_CALIDAD_ID = 9;
    private static final Integer ID_UNIDAD_DEFAULT = 1;
    private static final Integer STATUS_HABILITADO = 1;
    private static final Integer STATUS_DESHABILITADO = 0;

    private final ProcesarDeclaracionesPort procesarDeclaracionesPort;
    private final AlmacenPort almacenPort;

    @Override
    @Transactional
    public Mono<Integer> registrarDeclaracionProceso(List<ProcesoDeclararItemDTO> procesos, Integer idPartida) {
        log.info("Iniciando registro de declaración de procesos incompletos para partida ID: {}", idPartida);
        log.info("Total de procesos a declarar: {}", procesos.size());

        return procesarDeclaracionesPort.findProcesosByIdPartida(idPartida)
            .collectList()
            .flatMap(procesosDisponibles -> {
                log.info("Procesos disponibles encontrados: {}", procesosDisponibles.size());

                // Validar y filtrar procesos que existen en los disponibles
                List<ProcesoDeclararItemDTO> procesosValidos = validarProcesos(procesos, procesosDisponibles);
                log.info("Procesos válidos a guardar: {}", procesosValidos.size());

                if (procesosValidos.isEmpty()) {
                    log.warn("No hay procesos válidos para guardar");
                    return Mono.just(0);
                }

                // Guardar procesos válidos
                return guardarProcesosIncompletos(procesosValidos, idPartida, procesosDisponibles)
                    .flatMap(procesosGuardados -> {
                        log.info("Procesos guardados exitosamente: {}", procesosGuardados);

                        // Continuar con la creación de órdenes de ingreso en almacén de calidad
                        return crearOrdenIngresoCalidad(idPartida)
                            .thenReturn(procesosGuardados);
                    });
            })
            .doOnSuccess(total -> log.info("Proceso completo. Total de procesos registrados: {}", total))
            .doOnError(error -> log.error("Error en registro de declaración de procesos: {}", error.getMessage()));
    }

    /**
     * Valida que los procesos del request existan en los procesos disponibles.
     */
    private List<ProcesoDeclararItemDTO> validarProcesos(
            List<ProcesoDeclararItemDTO> procesos,
            List<DetailProcesoProductionDTO> procesosDisponibles) {

        Map<Integer, DetailProcesoProductionDTO> procesosMap = procesosDisponibles.stream()
            .collect(Collectors.toMap(DetailProcesoProductionDTO::getIdProceso, p -> p));

        return procesos.stream()
            .filter(proceso -> {
                boolean existe = procesosMap.containsKey(proceso.getIdProceso());
                if (!existe) {
                    log.warn("Proceso ID {} no encontrado en procesos disponibles, será omitido",
                        proceso.getIdProceso());
                }
                return existe;
            })
            .collect(Collectors.toList());
    }

    /**
     * Guarda los procesos incompletos en la base de datos.
     */
    private Mono<Integer> guardarProcesosIncompletos(
            List<ProcesoDeclararItemDTO> procesosValidos,
            Integer idPartida,
            List<DetailProcesoProductionDTO> procesosDisponibles) {

        Map<Integer, DetailProcesoProductionDTO> procesosMap = procesosDisponibles.stream()
            .collect(Collectors.toMap(DetailProcesoProductionDTO::getIdProceso, p -> p));

        return Flux.fromIterable(procesosValidos)
            .flatMap(proceso -> {
                DetailProcesoProductionDTO procesoDisponible = procesosMap.get(proceso.getIdProceso());

                // Si el proceso ya tiene idPartidaMaquina, significa que ya está registrado
                if (procesoDisponible.getIdPartidaMaquina() != null) {
                    log.info("Proceso ID {} ya está registrado con idPartidaMaquina {}, se omite",
                        proceso.getIdProceso(), procesoDisponible.getIdPartidaMaquina());
                    return Mono.just(0);
                }

                // Obtener idMaquina si es necesario
                return obtenerIdMaquina(proceso)
                    .flatMap(idMaquina -> {
                        ItemProcessProductionDTO procesoAGuardar = ItemProcessProductionDTO.builder()
                            .idDetRuta(proceso.getIdDetRuta())
                            .idProceso(proceso.getIdProceso())
                            .idTipoMaquina(proceso.getIdTipoMaquina())
                            .idMaquina(idMaquina)
                            .build();

                        return procesarDeclaracionesPort.saveProcesoIncompletoByIdPartida(procesoAGuardar, idPartida);
                    });
            })
            .reduce(0, Integer::sum)
            .doOnSuccess(total -> log.info("Total de procesos nuevos guardados: {}", total));
    }

    /**
     * Obtiene el ID de máquina para el proceso.
     * Si idTipoMaquina no es null, busca la primera máquina disponible.
     */
    private Mono<Integer> obtenerIdMaquina(ProcesoDeclararItemDTO proceso) {
        if (proceso.getIdTipoMaquina() == null) {
            log.debug("Proceso ID {} no tiene tipo de máquina, se usará null", proceso.getIdProceso());
            return Mono.justOrEmpty((Integer) null);
        }

        return procesarDeclaracionesPort.findFirstIdMachineByIdTipoMaquina(proceso.getIdTipoMaquina())
            .doOnSuccess(idMaquina -> log.debug("Máquina ID {} asignada para proceso ID {}",
                idMaquina, proceso.getIdProceso()))
            .onErrorResume(error -> {
                log.warn("No se encontró máquina para tipo {}, se usará null: {}",
                    proceso.getIdTipoMaquina(), error.getMessage());
                return Mono.justOrEmpty((Integer) null);
            });
    }

    /**
     * Crea la orden de ingreso en el almacén de calidad con los rollos de la partida.
     */
    private Mono<Void> crearOrdenIngresoCalidad(Integer idPartida) {
        log.info("Iniciando creación de orden de ingreso en almacén de calidad para partida ID: {}", idPartida);

        return Mono.zip(
                almacenPort.obtenerInfoPartida(idPartida),
                almacenPort.obtenerRollosAlmacenados(idPartida).collectList()
            )
            .flatMap(tuple -> {
                PartidaInfoProjection partidaInfo = tuple.getT1();
                List<RollsInStoreProjection> rollos = tuple.getT2();

                log.info("Información de partida obtenida: idCliente={}, idArticulo={}, lote={}",
                    partidaInfo.getId_cliente(), partidaInfo.getId_articulo(), partidaInfo.getLote());
                log.info("Rollos encontrados en almacenes: {}", rollos.size());

                if (rollos.isEmpty()) {
                    log.warn("No hay rollos en almacenes para mover a calidad");
                    return Mono.empty();
                }

                // Calcular peso total de los rollos
                BigDecimal pesoTotal = rollos.stream()
                    .map(RollsInStoreProjection::getPeso)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                log.info("Peso total de rollos: {} kg", pesoTotal);

                // Crear orden de ingreso (idOrigen = null para identificar proceso de corrección)
                return almacenPort.crearOrdenIngreso(
                        partidaInfo.getId_cliente(),
                        null,  // idOrigen = null (movimiento por corrección de procesos)
                        ALMACEN_CALIDAD_ID
                    )
                    .flatMap(idOrdenIngreso -> {
                        log.info("Orden de ingreso creada con ID: {}", idOrdenIngreso);

                        // Crear detalle de orden de ingreso
                        return almacenPort.crearDetalleOrdenIngreso(
                                idOrdenIngreso,
                                partidaInfo.getId_articulo(),
                                ID_UNIDAD_DEFAULT,
                                pesoTotal,
                                partidaInfo.getLote(),
                                rollos.size(),
                                idPartida
                            )
                            .flatMap(idDetOrdenIngreso -> {
                                log.info("Detalle de orden de ingreso creado con ID: {}", idDetOrdenIngreso);

                                // Crear detalles de peso para cada rollo y cambiar status de rollos antiguos
                                return crearDetallesPesoYActualizarRollos(
                                    idOrdenIngreso,
                                    idDetOrdenIngreso,
                                    rollos
                                );
                            });
                    });
            })
            .then()
            .doOnSuccess(v -> log.info("Orden de ingreso en almacén de calidad completada"))
            .doOnError(error -> log.error("Error creando orden de ingreso en almacén de calidad: {}",
                error.getMessage()));
    }

    /**
     * Crea los detalles de peso para cada rollo y actualiza el status de los rollos antiguos.
     */
    private Mono<Void> crearDetallesPesoYActualizarRollos(
            Integer idOrdenIngreso,
            Integer idDetOrdenIngreso,
            List<RollsInStoreProjection> rollos) {

        return Flux.fromIterable(rollos)
            .flatMap(rollo -> {
                // Crear nuevo detalle de peso en almacén de calidad (status = 1)
                BigDecimal pesoRollo = BigDecimal.valueOf(rollo.getPeso());

                return almacenPort.crearDetallePesoOrdenIngreso(
                        idOrdenIngreso,
                        rollo.getCodigo(),
                        pesoRollo,
                        idDetOrdenIngreso,
                        rollo.getIdRolloIngreso()
                    )
                    .flatMap(idDetallePeso -> {
                        log.info("Detalle de peso creado con ID: {} para rollo: {}",
                            idDetallePeso, rollo.getCodigo());

                        // Cambiar status del rollo anterior a 0 (deshabilitado)
                        return almacenPort.cambiarStatusRollo(
                            rollo.getIdDetOrdenIngresoPeso(),
                            STATUS_DESHABILITADO
                        );
                    })
                    .doOnSuccess(result -> log.info("Rollo {} movido a almacén de calidad, status anterior actualizado: {}",
                        rollo.getCodigo(), result));
            })
            .then()
            .doOnSuccess(v -> log.info("Todos los rollos procesados exitosamente"))
            .doOnError(error -> log.error("Error procesando detalles de peso y actualizando rollos: {}",
                error.getMessage()));
    }
}