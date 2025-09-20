package com.walrex.module_partidas.domain.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_partidas.application.ports.input.DeclineOutTachoUseCase;
import com.walrex.module_partidas.application.ports.output.OrdenSalidaPersistencePort;
import com.walrex.module_partidas.application.ports.output.SaveSuccessOutTachoPort;
import com.walrex.module_partidas.domain.model.*;
import com.walrex.module_partidas.domain.model.dto.IngresoAlmacenDTO;
import com.walrex.module_partidas.domain.model.dto.ItemRolloProcessDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación para DeclineOutTacho
 * Implementa el caso de uso y orquesta la lógica de negocio para rechazo de salida de tacho
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeclineOutTachoService implements DeclineOutTachoUseCase {

    private final SaveSuccessOutTachoPort saveSuccessOutTachoPort;
    private final OrdenSalidaPersistencePort ordenSalidaPersistencePort;
    private static final Integer ID_ALMACEN_RECHAZO = 6;

    @Override
    @Transactional
    public Mono<IngresoAlmacenDTO> declineOutTacho(DeclinePartidaTacho declinePartidaTacho) {
        log.info("Iniciando procesamiento de rechazo de salida de tacho para partida ID: {}",
                declinePartidaTacho.getIdPartida());

        List<ItemRolloProcess> rollosSeleccionados = declinePartidaTacho.getRollos().stream()
            .filter(rollo -> Boolean.TRUE.equals(rollo.getSelected()))
            .collect(Collectors.toList());

        if (rollosSeleccionados.isEmpty()) {
            log.error("No hay rollos seleccionados para partida ID: {}", declinePartidaTacho.getIdPartida());
            return Mono.error(new IllegalArgumentException("Debe seleccionar al menos un rollo"));
        }

        log.info("Procesando {} rollos seleccionados para rechazo en partida ID: {}", 
                rollosSeleccionados.size(), declinePartidaTacho.getIdPartida());

        return procesarRollosRechazoConTransaccion(declinePartidaTacho, rollosSeleccionados)
                .flatMap(ingresoAlmacen -> {
                    // Registrar motivo de rechazo
                    return registrarMotivoRechazo(declinePartidaTacho)
                            .thenReturn(ingresoAlmacen)
                            .doOnSuccess(result -> log.info("Procesamiento de rechazo completo exitoso para partida ID: {}", 
                                    declinePartidaTacho.getIdPartida()))
                            .doOnError(error -> log.error("Error registrando motivo de rechazo para partida ID: {} - Error: {}", 
                                    declinePartidaTacho.getIdPartida(), error.getMessage()));
                });
    }

    /**
     * Registra el motivo de rechazo en el sistema
     */
    private Mono<Void> registrarMotivoRechazo(DeclinePartidaTacho declinePartidaTacho) {
        log.info("Registrando motivo de rechazo para partida ID: {}", declinePartidaTacho.getIdPartida());
        
        String motivoRechazo = declinePartidaTacho.getMotivoRechazo().getValue() + " - " + 
                              declinePartidaTacho.getMotivoRechazo().getText();
        String personalSupervisor = declinePartidaTacho.getPersonal().getApenomEmpleado();
        String observacion = declinePartidaTacho.getObservacion();
        
        log.info("Registrando motivo de rechazo para partida {}: {} - Supervisor: {} - Observación: {}", 
                declinePartidaTacho.getIdPartida(), motivoRechazo, personalSupervisor, observacion);
        
        return Mono.<Void>empty()
                .doOnSuccess(v -> log.info("Motivo de rechazo registrado exitosamente"))
                .doOnError(error -> log.error("Error registrando motivo de rechazo: {}", error.getMessage()));
    }


    @Transactional
    private Mono<IngresoAlmacenDTO> procesarRollosRechazoConTransaccion(
            DeclinePartidaTacho declinePartidaTacho, 
            List<ItemRolloProcess> rollosSeleccionados) {
        return procesarRollosRechazo(declinePartidaTacho, rollosSeleccionados);
    }

    /**
     * Procesa los rollos de rechazo siguiendo el flujo de negocio
     */
    private Mono<IngresoAlmacenDTO> procesarRollosRechazo(DeclinePartidaTacho declinePartidaTacho,
            List<ItemRolloProcess> rollosSeleccionados) {

        return saveSuccessOutTachoPort
                .consultarRollosDisponibles(declinePartidaTacho.getIdPartida(), declinePartidaTacho.getIdAlmacen())
                .flatMap(rollosDisponibles -> {
                    if (rollosDisponibles.isEmpty()) {
                        log.error("No hay rollos disponibles para partida ID: {} en almacén ID: {}",
                                declinePartidaTacho.getIdPartida(), declinePartidaTacho.getIdAlmacen());
                        return Mono.error(new IllegalArgumentException(
                                "No hay rollos disponibles para procesar"));
                    }

                    log.info("Rollos disponibles encontrados: {} para partida ID: {} en almacén ID: {}",
                        rollosDisponibles.size(), declinePartidaTacho.getIdPartida(),
                        declinePartidaTacho.getIdAlmacen());

                    // Validar que todos los rollos seleccionados estén disponibles
                    return validarRollosSeleccionados(rollosSeleccionados, rollosDisponibles)
                                .then(procesarIngresoAlmacenRechazo(declinePartidaTacho,
                                    rollosSeleccionados, rollosDisponibles.size()));
                });
    }

    /**
     * Valida que todos los rollos seleccionados estén en la lista de rollos disponibles
     */
    private Mono<Void> validarRollosSeleccionados(List<ItemRolloProcess> rollosSeleccionados,
            List<ItemRollo> rollosDisponibles) {

        Map<Integer, ItemRollo> rollosDisponiblesMap = rollosDisponibles.stream()
            .collect(Collectors.toMap(
                ItemRollo::getIdIngresopeso,
                rollo -> rollo
            ));
        
        List<Integer> idsNoDisponibles = rollosSeleccionados.stream()
            .map(ItemRolloProcess::getIdIngresoPeso)
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
     * Procesa el ingreso al almacén de rechazo
     */
    private Mono<IngresoAlmacenDTO> procesarIngresoAlmacenRechazo(DeclinePartidaTacho declinePartidaTacho,
            List<ItemRolloProcess> rollosSeleccionados, Integer cntRollosAlmacen) {

        Integer idMotivoRechazo = Integer.parseInt(declinePartidaTacho.getMotivoRechazo().getValue());

        return saveSuccessOutTachoPort.crearOrdenIngresoRechazo(declinePartidaTacho.getIdCliente(), ID_ALMACEN_RECHAZO
                , idMotivoRechazo, declinePartidaTacho.getObservacion())
                .flatMap(idOrdenIngreso -> {
                    log.info("Orden de ingreso de rechazo creada con ID: {} para almacén ID: {}",
                        idOrdenIngreso, ID_ALMACEN_RECHAZO);

                    // Consultar información completa de la orden
                    return saveSuccessOutTachoPort.consultarOrdenIngresoCompleta(idOrdenIngreso)
                            .flatMap(ordenCompleta -> {
                                // Crear orden de salida correspondiente al ingreso
                                return crearOrdenSalida(declinePartidaTacho, idOrdenIngreso, ID_ALMACEN_RECHAZO)
                                        .flatMap(idOrdenSalida -> {
                                            log.info("Orden de salida de rechazo creada con ID: {} para orden de ingreso: {}", 
                                                idOrdenSalida, idOrdenIngreso);
                                            
                                            // Procesar cada rollo seleccionado y construir IngresoAlmacen
                                            return procesarRollosIndividuales(declinePartidaTacho, rollosSeleccionados, idOrdenIngreso, idOrdenSalida)
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
                                                            .codIngreso(ordenCompleta.getCodIngreso())
                                                            .idAlmacen(declinePartidaTacho.getIdAlmacen())
                                                            .idArticulo(declinePartidaTacho.getIdArticulo())
                                                            .idUnidad(declinePartidaTacho.getIdUnidad())
                                                            .cntRollos(rollosProcesados.size())
                                                            .pesoRef(pesoTotal)
                                                            .rollos(rollos)
                                                            .ingresos(idOrdenIngresoCntRollos)
                                                            .cntRollosAlmacen(cntRollosAlmacen)
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
    }

    /**
     * Valida y deshabilita órdenes de ingreso según el mapeo de cantidades procesadas
     */
    private Mono<Boolean> validarYDeshabilitarOrdenesIngreso(Map<Integer, Integer> idOrdenIngresoCntRollos, 
            int totalRollosSeleccionados, List<ItemRolloProcessDTO> rollosProcesados) {
        log.info("Iniciando validación de órdenes de ingreso de rechazo. Mapeo: {}, Total rollos seleccionados: {}", 
                idOrdenIngresoCntRollos, totalRollosSeleccionados);

        return Flux.fromIterable(idOrdenIngresoCntRollos.entrySet())
            .flatMap(entry -> {
                Integer idOrdenIngreso = entry.getKey();
                Integer cntRollosProcesados = entry.getValue();
                
                log.info("Validando orden de ingreso de rechazo ID: {} con {} rollos procesados", 
                        idOrdenIngreso, cntRollosProcesados);

                return validarCantidadRollosProcesados(idOrdenIngreso, cntRollosProcesados)
                        .flatMap(deshabilitarSiEsNecesario -> {
                            if (deshabilitarSiEsNecesario) {
                                log.info("Deshabilitando orden de ingreso de rechazo: {} - Cantidad procesada: {}", 
                                        idOrdenIngreso, cntRollosProcesados);
                                return saveSuccessOutTachoPort.deshabilitarOrdenIngreso(idOrdenIngreso)
                                .then(deshabilitarRollosDeOrden(idOrdenIngreso, rollosProcesados))
                                .thenReturn(cntRollosProcesados);
                            } else {
                                log.info("Orden de ingreso de rechazo {} no requiere deshabilitación - Cantidad procesada: {}", 
                                    idOrdenIngreso, cntRollosProcesados);
                            
                                // Aún así debemos deshabilitar los rollos individuales procesados
                                return deshabilitarRollosDeOrden(idOrdenIngreso, rollosProcesados)
                                        .thenReturn(cntRollosProcesados);
                            }
                        });
            })
            .collectList()
            .map(cantidadesProcesadas -> {
                int totalProcesado = cantidadesProcesadas.stream()
                        .mapToInt(Integer::intValue)
                        .sum();
                boolean esCompleto = totalProcesado == totalRollosSeleccionados;
                log.info("Cantidades procesadas: {}, Total rollos procesados: {}, Total seleccionados: {}, Es completo: {}", 
                        cantidadesProcesadas, totalProcesado, totalRollosSeleccionados, esCompleto);
                return esCompleto;
            })
            .doOnError(error -> log.error("Error durante validación de órdenes de rechazo: {}", error.getMessage()));
    }

    /**
     * Procesa cada rollo individualmente creando detalles y actualizando estados
     */
    private Mono<List<ItemRolloProcessDTO>> procesarRollosIndividuales(DeclinePartidaTacho declinePartidaTacho,
            List<ItemRolloProcess> rollosSeleccionados,
            Integer idOrdenIngreso,
            Integer idOrdenSalida) {

        Integer idArticulo = declinePartidaTacho.getIdArticulo();
        Integer idUnidad = declinePartidaTacho.getIdUnidad();
        String lote = declinePartidaTacho.getLote();

        // Calcular peso total de los rollos seleccionados
        Double pesoTotal = rollosSeleccionados.stream()
                .mapToDouble(rollo -> Double.valueOf(rollo.getPesoRollo()))
                .sum();

        log.info("Peso total calculado para crearDetalleOrdenIngresoRechazo: {} para {} rollos seleccionados", 
            pesoTotal, rollosSeleccionados.size());

        // Crear el detalle de orden de ingreso de rechazo UNA SOLA VEZ
        return saveSuccessOutTachoPort.crearDetalleOrdenIngreso(idOrdenIngreso, idArticulo, idUnidad,
                    BigDecimal.valueOf(pesoTotal), lote, rollosSeleccionados.size(), declinePartidaTacho.getIdPartida())
                .flatMap(idDetOrdenIngreso -> {
                    log.info("Detalle de orden de ingreso de rechazo creado con ID: {} para orden: {}",
                                    idDetOrdenIngreso, idOrdenIngreso);

                    return ordenSalidaPersistencePort.crearDetalleOrdenSalida(
                            idOrdenSalida,
                            idArticulo,
                            idUnidad,
                            rollosSeleccionados.size(),
                            declinePartidaTacho.getIdPartida(),
                            BigDecimal.valueOf(pesoTotal),
                            idDetOrdenIngreso
                    )
                    .flatMap(idDetalleOrdenSalida -> {
                        log.info("Detalle de orden de salida de rechazo creado con ID: {} para orden de salida: {}",
                                idDetalleOrdenSalida, idOrdenSalida);

                        return procesarRollosPeso(declinePartidaTacho, rollosSeleccionados, idOrdenIngreso,
                                        idDetOrdenIngreso, idOrdenSalida, idDetalleOrdenSalida)
                            .collectList();
                    });
                });
    }

    /**
     * Procesa los rollos de peso y actualiza sus estados
     */
    private Flux<ItemRolloProcessDTO> procesarRollosPeso(DeclinePartidaTacho declinePartidaTacho,
            List<ItemRolloProcess> rollosSeleccionados,
            Integer idOrdenIngreso,
            Integer idDetOrdenIngreso,
            Integer idOrdenSalida,
            Integer idDetalleOrdenSalida) {

        log.info("Iniciando procesamiento de {} rollos seleccionados para rechazo en partida ID: {}", 
                rollosSeleccionados.size(), declinePartidaTacho.getIdPartida());

        return Flux.fromIterable(rollosSeleccionados)
                    .flatMap(rollo -> procesarRolloIndividual(declinePartidaTacho, rollo, idOrdenIngreso, idDetOrdenIngreso, 
                            idOrdenSalida, idDetalleOrdenSalida))
                    .doOnComplete(() -> log.info(
                                "Todos los rollos de rechazo procesados exitosamente para partida ID: {}",
                                declinePartidaTacho.getIdPartida()
                            )
                    );
    }

    /**
     * Procesa un rollo individual y devuelve el rollo procesado
     */
    private Mono<ItemRolloProcessDTO> procesarRolloIndividual(DeclinePartidaTacho declinePartidaTacho,
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
                        log.debug("Detalle de peso de rechazo creado con ID: {} para rollo: {}", idDetPeso, rollo);
                        
                        // Crear detalle de peso de orden de salida
                        return ordenSalidaPersistencePort.crearDetOrdenSalidaPeso(
                                idDetalleOrdenSalida,
                                idOrdenSalida,
                                rollo.getCodRollo(),
                                pesoRollo,
                                rollo.getIdDetPartida(),
                                idRolloIngreso
                        )
                        .then(Mono.fromCallable(() -> {

                            ItemRolloProcessDTO itemRolloProcessDTO = ItemRolloProcessDTO.builder()
                                .codRollo(rollo.getCodRollo())
                                .pesoRollo(Double.valueOf(rollo.getPesoRollo()))
                                .idOrdenIngreso(idOrdenIngreso)
                                .idIngresoPeso(idDetPeso)
                                .idIngresoAlmacen(rollo.getIdIngresoAlmacen())
                                .idRolloIngreso(rollo.getIdIngresoPeso())
                                .idDetPartida(rollo.getIdDetPartida())
                                .idDetOrdenIngPesoAlmacen(rollo.getIdRolloIngreso())
                                .selected(rollo.getSelected())
                                .status(rollo.getStatus())
                                .build();
                            return itemRolloProcessDTO;
                        }));
                })
                .onErrorMap(throwable -> {
                    log.error("Error al procesar rollo de rechazo {}: {}", rollo.getCodRollo(), throwable.getMessage());
                    return new RuntimeException("Error al procesar rollo de rechazo " + rollo.getCodRollo() + ": " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Valida si todos los rollos fueron procesados
     */
    private Mono<Boolean> validarCantidadRollosProcesados(Integer idOrdenIngreso, Integer cantidadRollosProcesados) {
        return saveSuccessOutTachoPort.getCantidadRollosOrdenIngreso(idOrdenIngreso)
                .map(cantidadEnBD -> {
                    boolean todosProcesados = cantidadEnBD.equals(cantidadRollosProcesados);
                    
                    if (todosProcesados) {
                        log.info("Todos los rollos de rechazo procesados para orden: {}. Cantidad: {}", 
                            idOrdenIngreso, cantidadEnBD);
                    } else {
                        log.info("Aún quedan rollos por procesar en rechazo. Cantidad en BD: {}, Procesados: {}", 
                            cantidadEnBD, cantidadRollosProcesados);
                    }
                    
                    return todosProcesados;
                })
                .onErrorMap(throwable -> {
                    log.error("Error al validar cantidad de rollos de rechazo para orden {}: {}", idOrdenIngreso, throwable.getMessage());
                    return new RuntimeException("Error al validar cantidad de rollos de rechazo: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * Crea una orden de salida correspondiente al ingreso de rechazo
     */
    private Mono<Integer> crearOrdenSalida(DeclinePartidaTacho declinePartidaTacho, 
            Integer idOrdenIngreso, Integer idAlmacenDestino) {
        
        Integer idAlmacenOrigen = declinePartidaTacho.getIdAlmacen();
        Integer idUsuario = declinePartidaTacho.getIdUsuario();
        Integer idDocumentoRef = declinePartidaTacho.getIdPartida();
        OffsetDateTime fecRegistro = OffsetDateTime.now();
        
        log.info("Creando orden de salida de rechazo: almacén origen={}, almacén destino={}, usuario={}", 
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
        List<ItemRolloProcessDTO> rollosDeEstaOrden = rollosProcesados.stream()
                .filter(rollo -> idOrdenIngreso.equals(rollo.getIdIngresoAlmacen()))
                .collect(Collectors.toList());
        
        log.info("Deshabilitando {} rollos individuales de rechazo de la orden: {}", rollosDeEstaOrden.size(), idOrdenIngreso);
        
        return Flux.fromIterable(rollosDeEstaOrden)
                .flatMap(rollo -> {
                    Integer idDetOrdenIngresoPeso = rollo.getIdDetOrdenIngPesoAlmacen();
                    if (idDetOrdenIngresoPeso != null) {
                        log.debug("Deshabilitando rollo individual de rechazo: {} (idDetOrdenIngresoPeso: {})", 
                                rollo.getCodRollo(), idDetOrdenIngresoPeso);
                        return saveSuccessOutTachoPort.actualizarStatusDetallePeso(idDetOrdenIngresoPeso);
                    }
                    return Mono.empty();
                })
                .then()
                .doOnSuccess(v -> log.info("Rollos individuales de rechazo deshabilitados exitosamente para orden: {}", idOrdenIngreso))
                .doOnError(error -> log.error("Error deshabilitando rollos de rechazo de orden {}: {}", idOrdenIngreso, error.getMessage()));
    }
}
