package com.walrex.module_partidas.domain.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_partidas.application.ports.input.SaveSuccessOutTachoUseCase;
import com.walrex.module_partidas.application.ports.output.*;
import com.walrex.module_partidas.domain.model.*;
import com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto.WebSocketNotificationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación para SaveSuccessOutTacho
 * Implementa el caso de uso y orquesta la lógica de negocio
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaveSuccessOutTachoService implements SaveSuccessOutTachoUseCase {

        private final SaveSuccessOutTachoPort saveSuccessOutTachoPort;
        private final OrdenSalidaPersistencePort ordenSalidaPersistencePort;
        private final WebSocketNotificationPort webSocketNotificationPort;

        @Override
        @Transactional
        public Mono<IngresoAlmacen> saveSuccessOutTacho(SuccessPartidaTacho successPartidaTacho) {
                log.info("Iniciando procesamiento de salida exitosa de tacho para partida ID: {}",
                                successPartidaTacho.getIdPartida());

                List<ItemRolloProcess> rollosSeleccionados = successPartidaTacho.getRollos().stream()
                    .filter(rollo -> Boolean.TRUE.equals(rollo.getSelected()))
                    .collect(Collectors.toList());

                if (rollosSeleccionados.isEmpty()) {
                    log.error("No hay rollos seleccionados para partida ID: {}", successPartidaTacho.getIdPartida());
                    return Mono.error(new IllegalArgumentException("Debe seleccionar al menos un rollo"));
                }

                log.info("Procesando {} rollos seleccionados para partida ID: {}", rollosSeleccionados.size(),
                        successPartidaTacho.getIdPartida());

                return procesarRollosSeleccionados(successPartidaTacho, rollosSeleccionados);
        }

        /**
         * Envía notificación WebSocket de movimiento de partida
         */
        private Mono<Void> notificarMovimientoPartida(IngresoAlmacen ingresoAlmacen, SuccessPartidaTacho successPartidaTacho) {
            log.info("Enviando notificación de movimiento de partida");
            
            String roomName = (ingresoAlmacen.getIdAlmacen() != null) ? 
                    String.format("store-%d", ingresoAlmacen.getIdAlmacen()) : "";
            String storeOut = String.format("store-%d", successPartidaTacho.getIdAlmacen());
            
            WebSocketNotificationRequest request = WebSocketNotificationRequest.builder()
                    .roomName(roomName)
                    .operation("R")
                    .idOrdenIngreso(ingresoAlmacen.getIdOrdeningreso())
                    .codOrdenIngreso(ingresoAlmacen.getCodIngreso())
                    .storeOut(storeOut)
                    .idOrdenIngresoOut(successPartidaTacho.getIdPartida())
                    .build();
            
            return webSocketNotificationPort.enviarNotificacionAlmacen(request)
                    .doOnSuccess(v -> log.info("Notificación WebSocket enviada exitosamente"))
                    .doOnError(error -> log.error("Error enviando notificación WebSocket: {}", error.getMessage()));
        }        
        /**
         * Procesa los rollos seleccionados siguiendo el flujo de negocio
         */
        private Mono<IngresoAlmacen> procesarRollosSeleccionados(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados) {

            return saveSuccessOutTachoPort
                    .consultarRollosDisponibles(successPartidaTacho.getIdPartida(), successPartidaTacho.getIdAlmacen())
                    .flatMap(rollosDisponibles -> {
                        if (rollosDisponibles.isEmpty()) {
                            log.error("No hay rollos disponibles para partida ID: {} en almacén ID: {}",
                                    successPartidaTacho.getIdPartida(), successPartidaTacho.getIdAlmacen());
                            return Mono.error(new IllegalArgumentException(
                                    "No hay rollos disponibles para procesar"));
                        }

                        log.info("Rollos disponibles encontrados: {} para partida ID: {} en almacén ID: {}",
                            rollosDisponibles.size(), successPartidaTacho.getIdPartida(),
                            successPartidaTacho.getIdAlmacen());

                        // Validar que todos los rollos seleccionados estén disponibles
                        return validarRollosSeleccionados(rollosSeleccionados, rollosDisponibles)
                                    .then(procesarIngresoProximoAlmacen(successPartidaTacho,
                                        rollosSeleccionados));
                    });
        }

        /**
         * Valida que todos los rollos seleccionados estén en la lista de rollos
         * disponibles
         */
        private Mono<Void> validarRollosSeleccionados(List<ItemRolloProcess> rollosSeleccionados,
                        List<ItemRollo> rollosDisponibles) {

                Map<Integer, ItemRollo> rollosDisponiblesMap = rollosDisponibles.stream()
                    .collect(Collectors.toMap(
                        ItemRollo::getIdIngresopeso,
                        rollo -> rollo
                    ));
                
                List<Integer> idsNoDisponibles = rollosSeleccionados.stream()
                    .map(ItemRolloProcess::getIdIngresoPeso)  // Obtener idIngresoPeso
                    .filter(idIngresoPeso -> !rollosDisponiblesMap.containsKey(idIngresoPeso))
                    .collect(Collectors.toList());


                if (!idsNoDisponibles.isEmpty()) {
                    log.error("Rollos no disponibles encontrados por idIngresoPeso: {} para partida", idsNoDisponibles);
                    return Mono.error(new IllegalArgumentException(
                                    "Los siguientes rollos no están disponibles: " + idsNoDisponibles));
                }

                log.info("Todos los rollos seleccionados están disponibles");
                return Mono.empty();
        }

        /**
         * Procesa el ingreso al próximo almacén y actualiza los estados
         */
        private Mono<IngresoAlmacen> procesarIngresoProximoAlmacen(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados) {

            return saveSuccessOutTachoPort.consultarProcesosPartida(successPartidaTacho.getIdPartida())
                    .flatMap(procesos -> {
                        // Buscar el primer proceso pendiente (is_pendiente = true)
                        ProcesoPartida procesoPendiente = procesos.stream()
                            .filter(proceso -> Boolean.TRUE
                                .equals(proceso.getIsPendiente()))
                            .findFirst()
                            .orElse(null);

                        if (procesoPendiente == null) {
                            log.error("No hay procesos pendientes para partida ID: {}",
                                successPartidaTacho.getIdPartida());
                            return Mono.error(new IllegalArgumentException(
                                "No hay procesos pendientes para procesar"));
                        }

                        log.info("Proceso pendiente encontrado: {} para almacén ID: {}",
                            procesoPendiente.getNoProceso(),
                            procesoPendiente.getIdAlmacen());

                        return crearIngresoProximoAlmacen(successPartidaTacho, rollosSeleccionados,
                            procesoPendiente, procesoPendiente.getIdAlmacen());
                    });
        }

        /**
         * Crea el ingreso al próximo almacén y procesa todos los rollos
         */
        private Mono<IngresoAlmacen> crearIngresoProximoAlmacen(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados,
                        ProcesoPartida procesoPendiente, Integer idAlmacen) {

            return saveSuccessOutTachoPort
                    .crearOrdenIngreso(successPartidaTacho.getIdCliente(), idAlmacen)
                    .flatMap(idOrdenIngreso -> {
                        log.info("Orden de ingreso creada con ID: {} para almacén ID: {}",
                            idOrdenIngreso, idAlmacen);

                        // Consultar información completa de la orden (incluyendo cod_ingreso generado por trigger)
                        return saveSuccessOutTachoPort.consultarOrdenIngresoCompleta(idOrdenIngreso)
                                .flatMap(ordenCompleta -> {
                                    // Crear orden de salida correspondiente al ingreso
                                    return crearOrdenSalida(successPartidaTacho, idOrdenIngreso, idAlmacen)
                                            .flatMap(idOrdenSalida -> {
                                                log.info("Orden de salida creada con ID: {} para orden de ingreso: {}", 
                                                    idOrdenSalida, idOrdenIngreso);
                                                
                                                // Procesar cada rollo seleccionado y construir IngresoAlmacen
                                                return procesarRollosIndividuales(successPartidaTacho, rollosSeleccionados, idOrdenIngreso, idOrdenSalida)
                                                        .flatMap(rollosProcesados -> {
                                                // Convertir ItemRolloProcess a ItemRollo para la respuesta
                                                List<ItemRollo> rollos = rollosProcesados.stream()
                                                        .map(this::convertirAItemRollo)
                                                        .collect(Collectors.toList());

                                                // Calcular peso total de los rollos procesados
                                                Double pesoTotal = rollosProcesados.stream()
                                                        .mapToDouble(rollo -> Double.valueOf(rollo.getPesoRollo()))
                                                        .sum();
                                                
                                                log.info("Peso total calculado: {} para {} rollos procesados", pesoTotal, rollosProcesados.size());

                                                IngresoAlmacen ingresoAlmacen = IngresoAlmacen.builder()
                                                        .idOrdeningreso(ordenCompleta.getIdOrdeningreso())
                                                        .idCliente(ordenCompleta.getIdCliente())
                                                        .codIngreso(ordenCompleta.getCodIngreso()) // Código generado por trigger
                                                        .idAlmacen(idAlmacen)
                                                        .idArticulo(successPartidaTacho.getIdArticulo())
                                                        .idUnidad(successPartidaTacho.getIdUnidad())
                                                        .cntRollos(rollosProcesados.size())
                                                        .pesoRef(pesoTotal) // Peso total calculado de los rollos
                                                        .rollos(rollos)
                                                        .build();

                                                // Validar si todos los rollos fueron procesados y deshabilitar si es necesario
                                                if (rollos.size() == rollosSeleccionados.size()) {
                                                    log.info("Todos los rollos procesados. Deshabilitando orden de ingreso: {}", idOrdenIngreso);
                                                    return saveSuccessOutTachoPort.deshabilitarOrdenIngreso(idOrdenIngreso)
                                                            .thenReturn(ingresoAlmacen);
                                                } else {
                                                    log.info("Aún quedan rollos por procesar. Rollos procesados: {}, Rollos seleccionados: {}", 
                                                        rollos.size(), rollosSeleccionados.size());
                                                    return Mono.just(ingresoAlmacen);
                                                }
                                                        });
                                            });
                                });
                    });
        }

        /**
         * Procesa cada rollo individualmente creando detalles y actualizando estados
         */
        private Mono<List<ItemRolloProcess>> procesarRollosIndividuales(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados,
                        Integer idOrdenIngreso,
                        Integer idOrdenSalida
                        ) {

                // Usar los datos del dominio en lugar de valores por defecto
                Integer idArticulo = successPartidaTacho.getIdArticulo();
                Integer idUnidad = successPartidaTacho.getIdUnidad();

                // Calcular peso total de los rollos seleccionados
                Double pesoTotal = rollosSeleccionados.stream()
                        .mapToDouble(rollo -> Double.valueOf(rollo.getPesoRollo()))
                        .sum();

                log.info("Peso total calculado para crearDetalleOrdenIngreso: {} para {} rollos seleccionados", 
                    pesoTotal, rollosSeleccionados.size());

                // PRIMERO: Crear el detalle de orden de ingreso UNA SOLA VEZ
                return saveSuccessOutTachoPort.crearDetalleOrdenIngreso(idOrdenIngreso, idArticulo, idUnidad,
                            BigDecimal.valueOf(pesoTotal), BigDecimal.valueOf(rollosSeleccionados.size()), successPartidaTacho.getIdPartida())
                            .flatMap(idDetOrdenIngreso -> {
                                log.info("Detalle de orden de ingreso creado con ID: {} para orden: {}",
                                                idDetOrdenIngreso,
                                                idOrdenIngreso);

                                return ordenSalidaPersistencePort.crearDetalleOrdenSalida(
                                        idOrdenSalida,
                                        idArticulo,
                                        idUnidad,
                                        rollosSeleccionados.size(),
                                        successPartidaTacho.getIdPartida(),
                                        BigDecimal.valueOf(pesoTotal),
                                        idDetOrdenIngreso
                                )
                                .flatMap(idDetalleOrdenSalida -> {
                                    log.info("Detalle de orden de salida creado con ID: {} para orden de salida: {}",
                                            idDetalleOrdenSalida, idOrdenSalida);

                                    // TERCERO: Procesar el Flux de rollos con el ID del detalle ya creado
                                    return procesarRollosPeso(successPartidaTacho, rollosSeleccionados, idOrdenIngreso,
                                                    idDetOrdenIngreso, idOrdenSalida, idDetalleOrdenSalida)
                                        .collectList()
                                        .flatMap(rollosProcesados -> 
                                            validarCantidadRollosProcesados(idOrdenIngreso, rollosSeleccionados.size())
                                                .flatMap(deshabilitarSiEsNecesario -> {
                                                    if (deshabilitarSiEsNecesario) {
                                                        log.info("Deshabilitando orden de ingreso: {}", idOrdenIngreso);
                                                        return saveSuccessOutTachoPort.deshabilitarOrdenIngreso(idOrdenIngreso)
                                                                .thenReturn(rollosProcesados);
                                                    } else {
                                                        return Mono.just(rollosProcesados);
                                                    }
                                                })
                                        );
                                });
                            });
        }

        /**
         * Procesa los rollos de peso y actualiza sus estados
         */
        private Flux<ItemRolloProcess> procesarRollosPeso(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados,
                        Integer idOrdenIngreso,
                        Integer idDetOrdenIngreso,
                        Integer idOrdenSalida,
                        Integer idDetalleOrdenSalida) {

                return Flux.fromIterable(rollosSeleccionados)
                            .flatMap(rollo -> procesarRolloIndividual(successPartidaTacho, rollo, idOrdenIngreso, idDetOrdenIngreso, 
                                    idOrdenSalida, idDetalleOrdenSalida))
                            .doOnComplete(() -> log.info(
                                        "Todos los rollos procesados exitosamente para partida ID: {}",
                                        successPartidaTacho.getIdPartida()
                                    )
                            );
        }

        /**
         * Procesa un rollo individual y devuelve el rollo procesado
         */
        private Mono<ItemRolloProcess> procesarRolloIndividual(SuccessPartidaTacho successPartidaTacho,
                        ItemRolloProcess rollo,
                        Integer idOrdenIngreso,
                        Integer idDetOrdenIngreso,
                        Integer idOrdenSalida,
                        Integer idDetalleOrdenSalida) {

            BigDecimal pesoRollo = new BigDecimal(rollo.getPesoRollo());
            Integer idRolloIngreso = rollo.getIdIngresoPeso();

            return saveSuccessOutTachoPort.crearDetallePesoOrdenIngreso(idOrdenIngreso, rollo.getCodRollo(),
                    pesoRollo, idDetOrdenIngreso, idRolloIngreso)
                    .flatMap(idDetPeso -> {
                            log.debug("Detalle de peso creado con ID: {} para rollo: {}", idDetPeso,
                                            rollo.getCodRollo());
                            
                            // Crear detalle de peso de orden de salida
                            return ordenSalidaPersistencePort.crearDetOrdenSalidaPeso(
                                    idDetalleOrdenSalida,
                                    idOrdenSalida,
                                    rollo.getCodRollo(),
                                    pesoRollo,
                                    rollo.getIdDetPartida(), // Usar idPartida como idDetPartida
                                    idRolloIngreso
                            )
                            .then(Mono.fromCallable(() -> {
                                ItemRolloProcess itemRolloProcess = ItemRolloProcess.builder()
                                    .idIngresoPeso(idDetPeso)
                                    .codRollo(rollo.getCodRollo())
                                    .pesoRollo(Double.valueOf(rollo.getPesoRollo()))
                                    .idRolloIngreso(rollo.getIdRolloIngreso())
                                    .build();
                                return itemRolloProcess;
                            }))
                            .flatMap(itemRolloProcess -> {
                                // Actualizar el status del rollo original a 0 (inactivo)
                                // TODO: ACTUALIZAR A STATUS CERO LOS ROLLOS CON id_rollo_ingreso (idRolloIngreso)
                                Integer idDetOrdenIngresoPeso = rollo.getIdRolloIngreso();
                                return saveSuccessOutTachoPort
                                                .actualizarStatusDetallePeso(idDetOrdenIngresoPeso)
                                                .thenReturn(itemRolloProcess);
                            });
                    })
                    .onErrorMap(throwable -> {
                        log.error("Error al procesar rollo {}: {}", rollo.getCodRollo(), throwable.getMessage());
                        return new RuntimeException("Error al procesar rollo " + rollo.getCodRollo() + ": " + throwable.getMessage(), throwable);
                    });
        }

        /**
         * Valida si todos los rollos fueron procesados
         * @param idOrdenIngreso ID de la orden de ingreso
         * @param cantidadRollosProcesados Cantidad de rollos que se procesaron
         * @return Mono<Boolean> true si todos los rollos fueron procesados, false en caso contrario
         */
        private Mono<Boolean> validarCantidadRollosProcesados(Integer idOrdenIngreso, Integer cantidadRollosProcesados) {
            return saveSuccessOutTachoPort.getCantidadRollosOrdenIngreso(idOrdenIngreso)
                    .map(cantidadEnBD -> {
                        boolean todosProcesados = cantidadEnBD.equals(cantidadRollosProcesados);
                        
                        if (todosProcesados) {
                            log.info("Todos los rollos procesados para orden: {}. Cantidad: {}", 
                                idOrdenIngreso, cantidadEnBD);
                        } else {
                            log.info("Aún quedan rollos por procesar. Cantidad en BD: {}, Procesados: {}", 
                                cantidadEnBD, cantidadRollosProcesados);
                        }
                        
                        return todosProcesados;
                    })
                    .onErrorMap(throwable -> {
                        log.error("Error al validar cantidad de rollos para orden {}: {}", idOrdenIngreso, throwable.getMessage());
                        return new RuntimeException("Error al validar cantidad de rollos: " + throwable.getMessage(), throwable);
                    });
        }

        /**
         * Crea una orden de salida correspondiente al ingreso
         */
        private Mono<Integer> crearOrdenSalida(SuccessPartidaTacho successPartidaTacho, 
                Integer idOrdenIngreso, Integer idAlmacenDestino) {
            
            // Usar el almacén origen de la partida (almacén actual)
            Integer idAlmacenOrigen = successPartidaTacho.getIdAlmacen();
            
            // Usar el supervisor de la partida como usuario
            Integer idUsuario = successPartidaTacho.getIdSupervisor();
            
            // Usar la orden de ingreso como documento de referencia
            Integer idDocumentoRef = successPartidaTacho.getIdPartida();
            
            // Fecha actual
            OffsetDateTime fecRegistro = OffsetDateTime.now();
            
            log.info("Creando orden de salida: almacén origen={}, almacén destino={}, usuario={}", 
                idAlmacenOrigen, idAlmacenDestino, idUsuario);
            
            return ordenSalidaPersistencePort.crearOrdenSalida(
                    idAlmacenOrigen,
                    idAlmacenDestino,
                    fecRegistro,
                    idUsuario,
                    idDocumentoRef
            );
        }

        /**
         * Convierte ItemRolloProcess a ItemRollo para la respuesta
         */
        private ItemRollo convertirAItemRollo(ItemRolloProcess itemRolloProcess) {
            return ItemRollo.builder()
                    .idIngresopeso(itemRolloProcess.getIdIngresoPeso())
                    .idRolloIngreso(itemRolloProcess.getIdRolloIngreso())
                    .codRollo(itemRolloProcess.getCodRollo())
                    .pesoRollo(Double.valueOf(itemRolloProcess.getPesoRollo()))
                    .build();
        }
}
