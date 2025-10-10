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
import com.walrex.module_partidas.domain.model.dto.*;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoDocumentoEntity;
import com.walrex.module_partidas.infrastructure.adapters.outbound.websocket.dto.WebSocketNotificationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación para SaveSuccessOutTacho
 * Implementa el caso de uso y orquesta la lógica de negocio
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaveSuccessOutTachoService implements SaveSuccessOutTachoUseCase {

        private final SaveSuccessOutTachoPort saveSuccessOutTachoPort;
        private final OrdenSalidaPersistencePort ordenSalidaPersistencePort;
        private final WebSocketNotificationPort webSocketNotificationPort;

        @Override
        public Mono<IngresoAlmacenDTO> saveSuccessOutTacho(SuccessPartidaTacho successPartidaTacho) {
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

                return procesarRollosSeleccionadosConTransaccion(successPartidaTacho, rollosSeleccionados)
                        .flatMap(ingresoAlmacen -> {
                            // Enviar notificación WebSocket después de que todo sea exitoso
                            return notificarMovimientoPartida(ingresoAlmacen, successPartidaTacho)
                                    .thenReturn(ingresoAlmacen)
                                    .doOnSuccess(result -> log.info("Procesamiento completo exitoso para partida ID: {}",
                                            successPartidaTacho.getIdPartida()))
                                    .doOnError(error -> log.error("Error en notificación WebSocket para partida ID: {} - Error: {}",
                                            successPartidaTacho.getIdPartida(), error.getMessage()));
                        });
        }

        /**
         * Envía notificación WebSocket de movimiento de partida
         */
        private Mono<Void> notificarMovimientoPartida(IngresoAlmacenDTO ingresoAlmacenDTO, SuccessPartidaTacho successPartidaTacho) {
            log.info("Enviando notificación de movimiento de partida");

            String roomName = (ingresoAlmacenDTO.getIdAlmacen() != null) ?
                    String.format("store-%d", ingresoAlmacenDTO.getIdAlmacen()) : "";
            String storeOut = String.format("store-%d", successPartidaTacho.getIdAlmacen());

            WebSocketNotificationRequest request = WebSocketNotificationRequest.builder()
                    .roomName(roomName)
                    .operation("R")
                    .idOrdenIngreso(ingresoAlmacenDTO.getIdOrdeningreso())
                    .codOrdenIngreso(ingresoAlmacenDTO.getCodIngreso())
                    .storeOut(storeOut)
                    .idOrdenIngresoOut(successPartidaTacho.getIdPartida())
                    .build();

            return webSocketNotificationPort.enviarNotificacionAlmacen(request)
                    .doOnSuccess(v -> log.info("Notificación WebSocket enviada exitosamente"))
                    .doOnError(error -> log.error("Error enviando notificación WebSocket: {}", error.getMessage()));
        }

        @Transactional
        private Mono<IngresoAlmacenDTO> procesarRollosSeleccionadosConTransaccion(
                SuccessPartidaTacho successPartidaTacho,
                List<ItemRolloProcess> rollosSeleccionados) {
            return procesarRollosSeleccionados(successPartidaTacho, rollosSeleccionados);
        }

        /**
         * Procesa los rollos seleccionados siguiendo el flujo de negocio
         */
        private Mono<IngresoAlmacenDTO> procesarRollosSeleccionados(SuccessPartidaTacho successPartidaTacho,
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
                                        rollosSeleccionados, rollosDisponibles.size()));
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
        private Mono<IngresoAlmacenDTO> procesarIngresoProximoAlmacen(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados, Integer cntRollosAlmacen) {

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
                            procesoPendiente, procesoPendiente.getIdAlmacen(), cntRollosAlmacen);
                    });
        }

        /**
         * Crea el ingreso al próximo almacén y procesa todos los rollos
         */
        private Mono<IngresoAlmacenDTO> crearIngresoProximoAlmacen(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados,
                        ProcesoPartida procesoPendiente, Integer idAlmacen, Integer cntRollosAlmacen) {

            return saveSuccessOutTachoPort
                    .crearOrdenIngreso(successPartidaTacho.getIdCliente(), successPartidaTacho.getIdAlmacen(), idAlmacen)
                    .flatMap(idOrdenIngreso -> {
                        log.info("Orden de ingreso creada con ID: {} para almacén ID: {}",
                            idOrdenIngreso, idAlmacen);

                        // Consultar información completa de la orden (incluyendo cod_ingreso generado por trigger)
                        return saveSuccessOutTachoPort.consultarOrdenIngresoCompleta(idOrdenIngreso)
                                .flatMap(ordenCompleta -> {
                                    // Crear orden de salida correspondiente al ingreso
                                    return crearOrdenSalida(successPartidaTacho, idOrdenIngreso, idAlmacen)
                                            .flatMap(idOrdenSalida -> {
                                                IngresoDocumentoDTO ingresoDocumentoDTO = IngresoDocumentoDTO.builder()
                                                    .id_ordeningreso(idOrdenIngreso)
                                                    .id_tipo_documento(3)
                                                    .id_documento(successPartidaTacho.getIdPartida())
                                                    .id_almacen(idAlmacen)
                                                    .build();
                                                
                                                log.info("Orden de salida creada con ID: {} para orden de ingreso: {}",
                                                    idOrdenSalida, idOrdenIngreso);

                                                // Solo guardar documento de ingreso si todos los rollos del almacén fueron seleccionados
                                                Mono<OrdenIngresoDocumentoEntity> documentoIngresoMono;
                                                if (rollosSeleccionados.size() == cntRollosAlmacen) {
                                                    log.info("Todos los rollos del almacén fueron seleccionados ({}), guardando documento de ingreso", cntRollosAlmacen);
                                                    documentoIngresoMono = saveDocumentoIngreso(ingresoDocumentoDTO);
                                                } else {
                                                    log.info("No todos los rollos fueron seleccionados ({} de {}), omitiendo documento de ingreso", 
                                                        rollosSeleccionados.size(), cntRollosAlmacen);
                                                    documentoIngresoMono = Mono.just(OrdenIngresoDocumentoEntity.builder().build());
                                                }

                                                // Procesar cada rollo seleccionado y construir IngresoAlmacen
                                                return documentoIngresoMono
                                                        .flatMap(documentoIngreso -> {
                                                            return procesarRollosIndividuales(successPartidaTacho, rollosSeleccionados, idOrdenIngreso, idOrdenSalida)
                                                                .flatMap(rollosProcesados -> {
                                                                    // Obtener el mapeo de id_ordeningreso y cantidad de rollos procesados
                                                                    Map<Integer, Integer> idOrdenIngresoCntRollos = rollosProcesados.stream()
                                                                            .filter(rollo -> rollo.getIdIngresoAlmacen() != null)
                                                                            .collect(Collectors.groupingBy(
                                                                                    ItemRolloProcessDTO::getIdIngresoAlmacen,
                                                                                    Collectors.collectingAndThen(
                                                                                            Collectors.counting(),
                                                                                            Math::toIntExact
                                                                                    )
                                                                            ));
        
                                                                    log.info("Rollos procesados: {} - Mapeo ID orden ingreso -> cnt rollos: {}",
                                                                            rollosProcesados.size(), idOrdenIngresoCntRollos);
        
                                                                    // Convertir ItemRolloProcess a ItemRollo para la respuesta
                                                                    List<ItemRollo> rollos = rollosProcesados.stream()
                                                                            .map(this::convertirAItemRollo)
                                                                            .collect(Collectors.toList());
        
                                                                    // Calcular peso total de los rollos procesados
                                                                    Double pesoTotal = rollosProcesados.stream()
                                                                            .mapToDouble(rollo -> Double.valueOf(rollo.getPesoRollo()))
                                                                            .sum();
        
                                                                    log.info("Peso total calculado: {} para {} rollos procesados", pesoTotal, rollosProcesados.size());
        
                                                                    IngresoAlmacenDTO ingresoAlmacenDTO = IngresoAlmacenDTO.builder()
                                                                        .idOrdeningreso(ordenCompleta.getIdOrdeningreso())
                                                                        .idCliente(ordenCompleta.getIdCliente())
                                                                        .codIngreso(ordenCompleta.getCodIngreso()) // Código generado por trigger
                                                                        .idAlmacen(idAlmacen)
                                                                        .idArticulo(successPartidaTacho.getIdArticulo())
                                                                        .idUnidad(successPartidaTacho.getIdUnidad())
                                                                        .cntRollos(rollosProcesados.size())
                                                                        .pesoRef(pesoTotal) // Peso total calculado de los rollos
                                                                        .rollos(rollos)
                                                                        .ingresos(idOrdenIngresoCntRollos)
                                                                        .cntRollosAlmacen(cntRollosAlmacen) // Total de rollos disponibles en el almacén
                                                                        .build();
        
                                                                    // Recorrer idOrdenIngresoCntRollos para validar cantidades y deshabilitar según índice
                                                                    return validarYDeshabilitarOrdenesIngreso(idOrdenIngresoCntRollos, rollosSeleccionados.size(), rollosProcesados)
                                                                            .doOnNext(esCompleto -> {
                                                                                log.info("Validación completada. Todos los rollos procesados: {}", esCompleto);
                                                                            })
                                                                            .thenReturn(ingresoAlmacenDTO);
                                                            });
                                                        });
                                            });
                                });
                    });
        }

        private Mono<OrdenIngresoDocumentoEntity> saveDocumentoIngreso(IngresoDocumentoDTO ingresoDocumentoDTO){
            return saveSuccessOutTachoPort.addDocumentoIngreso(ingresoDocumentoDTO);
        }

        /**
         * Valida y deshabilita órdenes de ingreso según el mapeo de cantidades procesadas
         *
         * @param idOrdenIngresoCntRollos Mapeo de id_ordeningreso -> cantidad de rollos procesados
         * @param totalRollosSeleccionados Total de rollos seleccionados para procesar
         * @return Mono<Boolean> true si el total de rollos procesados es igual al total seleccionado
         */
        private Mono<Boolean> validarYDeshabilitarOrdenesIngreso(Map<Integer, Integer> idOrdenIngresoCntRollos, int totalRollosSeleccionados, List<ItemRolloProcessDTO> rollosProcesados) {
            log.info("Iniciando validación de órdenes de ingreso. Mapeo: {}, Total rollos seleccionados: {}",
                    idOrdenIngresoCntRollos, totalRollosSeleccionados);

            // Crear un Flux de todas las validaciones y deshabilitaciones
            return Flux.fromIterable(idOrdenIngresoCntRollos.entrySet())
                .flatMap(entry -> {
                    Integer idOrdenIngreso = entry.getKey();
                    Integer cntRollosProcesados = entry.getValue();

                    log.info("Validando orden de ingreso ID: {} con {} rollos procesados",
                            idOrdenIngreso, cntRollosProcesados);

                    return validarCantidadRollosProcesados(idOrdenIngreso, cntRollosProcesados)
                            .flatMap(deshabilitarSiEsNecesario -> {
                                if (deshabilitarSiEsNecesario) {
                                    log.info("Deshabilitando orden de ingreso: {} - Cantidad procesada: {}",
                                            idOrdenIngreso, cntRollosProcesados);
                                            return saveSuccessOutTachoPort.deshabilitarOrdenIngreso(idOrdenIngreso)
                                                .then(saveSuccessOutTachoPort.deshabilitarDetalleIngreso(idOrdenIngreso))
                                                .then(deshabilitarRollosDeOrden(idOrdenIngreso, rollosProcesados))
                                                .thenReturn(cntRollosProcesados);
                                } else {
                                    log.info("Orden de ingreso {} no requiere deshabilitación - Cantidad procesada: {}",
                                        idOrdenIngreso, cntRollosProcesados);
                                    // Aún así debemos deshabilitar los rollos individuales procesados
                                    return deshabilitarRollosDeOrden(idOrdenIngreso, rollosProcesados)
                                            .thenReturn(cntRollosProcesados);
                                }
                            });
                })
                .collectList() // Recolecta todos los valores en una lista
                .map(cantidadesProcesadas -> {
                    int totalProcesado = cantidadesProcesadas.stream()
                            .mapToInt(Integer::intValue)
                            .sum();
                    boolean esCompleto = totalProcesado == totalRollosSeleccionados;
                    log.info("Cantidades procesadas: {}, Total rollos procesados: {}, Total seleccionados: {}, Es completo: {}",
                            cantidadesProcesadas, totalProcesado, totalRollosSeleccionados, esCompleto);
                    return esCompleto;
                })
                .doOnError(error -> log.error("Error durante validación de órdenes: {}", error.getMessage()));
        }

        /**
         * Procesa cada rollo individualmente creando detalles y actualizando estados
         */
        private Mono<List<ItemRolloProcessDTO>> procesarRollosIndividuales(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados,
                        Integer idOrdenIngreso,
                        Integer idOrdenSalida
                        ) {

                // Usar los datos del dominio en lugar de valores por defecto
                Integer idArticulo = successPartidaTacho.getIdArticulo();
                Integer idUnidad = successPartidaTacho.getIdUnidad();
                String lote = successPartidaTacho.getLote();

                // Calcular peso total de los rollos seleccionados
                Double pesoTotal = rollosSeleccionados.stream()
                        .mapToDouble(rollo -> Double.valueOf(rollo.getPesoRollo()))
                        .sum();

                log.info("Peso total calculado para crearDetalleOrdenIngreso: {} para {} rollos seleccionados",
                    pesoTotal, rollosSeleccionados.size());

                // PRIMERO: Crear el detalle de orden de ingreso UNA SOLA VEZ
                return saveSuccessOutTachoPort.crearDetalleOrdenIngreso(idOrdenIngreso, idArticulo, idUnidad,
                            BigDecimal.valueOf(pesoTotal), lote, rollosSeleccionados.size(), successPartidaTacho.getIdPartida())
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

                                    return procesarRollosPeso(successPartidaTacho, rollosSeleccionados, idOrdenIngreso,
                                                    idDetOrdenIngreso, idOrdenSalida, idDetalleOrdenSalida)
                                        .collectList();
                                });
                            });
        }

        /**
         * Procesa los rollos de peso y actualiza sus estados
         */
        private Flux<ItemRolloProcessDTO> procesarRollosPeso(SuccessPartidaTacho successPartidaTacho,
                        List<ItemRolloProcess> rollosSeleccionados,
                        Integer idOrdenIngreso,
                        Integer idDetOrdenIngreso,
                        Integer idOrdenSalida,
                        Integer idDetalleOrdenSalida) {

                log.info("Iniciando procesamiento de {} rollos seleccionados para partida ID: {}",
                        rollosSeleccionados.size(), successPartidaTacho.getIdPartida());

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
        private Mono<ItemRolloProcessDTO> procesarRolloIndividual(SuccessPartidaTacho successPartidaTacho,
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
                                            rollo);

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

                                ItemRolloProcessDTO itemRolloProcessDTO = ItemRolloProcessDTO.builder()
                                    .codRollo(rollo.getCodRollo())
                                    .pesoRollo(Double.valueOf(rollo.getPesoRollo()))
                                    .idOrdenIngreso(idOrdenIngreso)
                                    .idIngresoPeso(idDetPeso)
                                    .idIngresoAlmacen(rollo.getIdIngresoAlmacen())
                                    .idRolloIngreso(rollo.getIdIngresoPeso())//id_detordeningresopeso almacen crudo
                                    .idDetPartida(rollo.getIdDetPartida())
                                    .idDetOrdenIngPesoAlmacen(rollo.getIdRolloIngreso())//id_detordeningresopeso almacen produccion
                                    .selected(rollo.getSelected())
                                    .status(rollo.getStatus())
                                    .build();
                                return itemRolloProcessDTO;
                            }));
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
        private ItemRollo convertirAItemRollo(ItemRolloProcessDTO itemRolloProcess) {
            return ItemRollo.builder()
                    .idIngresopeso(itemRolloProcess.getIdIngresoPeso())
                    .idRolloIngreso(itemRolloProcess.getIdRolloIngreso())
                    .codRollo(itemRolloProcess.getCodRollo())
                    .pesoRollo(Double.valueOf(itemRolloProcess.getPesoRollo()))
                    .build();
        }

    /**
     * Deshabilita los rollos individuales de una orden específica
     */
    private Mono<Void> deshabilitarRollosDeOrden(Integer idOrdenIngreso, List<ItemRolloProcessDTO> rollosProcesados) {
        // Filtrar solo los rollos de esta orden específica
        List<ItemRolloProcessDTO> rollosDeEstaOrden = rollosProcesados.stream()
                .filter(rollo -> idOrdenIngreso.equals(rollo.getIdIngresoAlmacen()))
                .collect(Collectors.toList());

        log.info("Deshabilitando {} rollos individuales de la orden: {}", rollosDeEstaOrden.size(), idOrdenIngreso);

        return Flux.fromIterable(rollosDeEstaOrden)
                .flatMap(rollo -> {
                    Integer idDetOrdenIngresoPeso = rollo.getIdDetOrdenIngPesoAlmacen();//idDetOrdenIngPesoAlmacen
                    if (idDetOrdenIngresoPeso != null) {
                        log.debug("Deshabilitando rollo individual: {} (idDetOrdenIngresoPeso: {})",
                                rollo.getCodRollo(), idDetOrdenIngresoPeso);
                        return saveSuccessOutTachoPort.actualizarStatusDetallePeso(idDetOrdenIngresoPeso);
                    }
                    return Mono.empty();
                })
                .then()
                .doOnSuccess(v -> log.info("Rollos individuales deshabilitados exitosamente para orden: {}", idOrdenIngreso))
                .doOnError(error -> log.error("Error deshabilitando rollos de orden {}: {}", idOrdenIngreso, error.getMessage()));
    }
}
