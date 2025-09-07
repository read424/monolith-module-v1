package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.walrex.module_partidas.application.ports.output.ConsultarDetalleIngresoPort;
import com.walrex.module_partidas.domain.model.DetalleIngresoRollos;
import com.walrex.module_partidas.domain.model.ItemRollo;
import com.walrex.module_partidas.domain.model.dto.ConsultarDetalleIngresoRequest;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper.DetalleIngresoMapper;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.DetalleIngresoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para Detalle de Ingreso
 * Implementa el puerto de salida y se encarga de la comunicación con la base de
 * datos
 * 
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DetalleIngresoPersistenceAdapter implements ConsultarDetalleIngresoPort {

        private final DetalleIngresoRepository repository;
        private final DetalleIngresoMapper detalleIngresoMapper;

        @Override
        public Mono<DetalleIngresoRollos> consultarDetalleIngreso(ConsultarDetalleIngresoRequest request) {
                log.debug("Consultando detalle de ingreso para partida ID: {} en almacén ID: {}",
                                request.getIdPartida(), request.getIdAlmacen());
                // Consultar el detalle de ingreso principal y hacer reduce
                return repository.findDetalleIngreso(request.getIdAlmacen(), request.getIdPartida())
                                .collectList()
                                .flatMap(detalleProjections -> {
                                    if (detalleProjections.isEmpty()) {
                                        return Mono.empty();
                                    }
                                    // Hacer reduce de las proyecciones usando programación reactiva
                                    return Flux.fromIterable(detalleProjections)
                                            .reduce(new DetalleIngresoRollos(), (acumulador, proyeccion) -> {
                                                // Inicializar con datos del primer registro
                                                if (acumulador.getIdArticulo() == null) {
                                                    acumulador.setIdArticulo(proyeccion.getIdArticulo());
                                                    acumulador.setCodArticulo(proyeccion.getCodArticulo());
                                                    acumulador.setDescArticulo(proyeccion.getDescArticulo());
                                                    acumulador.setLote(proyeccion.getLote());
                                                    acumulador.setIdTipoProducto(proyeccion.getIdTipoProducto());
                                                    acumulador.setIdUnidad(proyeccion.getIdUnidad());
                                                    acumulador.setAbrevUnidad(proyeccion.getAbrevUnidad());
                                                    acumulador.setIdDetordeningreso(new ArrayList<Integer>());
                                                    acumulador.setIdOrdeningreso(new ArrayList<Integer>());
                                                    acumulador.setCntRollos(0);
                                                }
                                                // Sumar cntRollos
                                                acumulador.setCntRollos(acumulador.getCntRollos() + proyeccion.getCntRollos());
                                                // Agregar IDs a las listas (evitar duplicados)
                                                if (!acumulador.getIdDetordeningreso().contains(proyeccion.getIdDetordeningreso())) {
                                                    acumulador.getIdDetordeningreso().add(proyeccion.getIdDetordeningreso());
                                                }
                                                if (!acumulador.getIdOrdeningreso().contains(proyeccion.getIdOrdeningreso())) {
                                                    acumulador.getIdOrdeningreso().add(proyeccion.getIdOrdeningreso());
                                                }
                                                return acumulador;
                                            })
                                            .flatMap(detalleConsolidado -> {
                                                // Consultar los rollos correspondientes
                                                return repository
                                                            .findRollosByPartida(request.getIdPartida(), request.getIdAlmacen())
                                                            .collectList()
                                                            .map(rollosProjections -> {
                                                                // Mapear los rollos a dominio
                                                                List<ItemRollo> rollos = detalleIngresoMapper.toDomainList(rollosProjections);
                                                                // Asignar los rollos al detalle consolidado
                                                                detalleConsolidado.setRollos(rollos);
                                                                log.debug("Detalle de ingreso consolidado: ID={}, Rollos encontrados={}, Total cntRollos={}",
                                                                    detalleConsolidado.getIdDetordeningreso(),
                                                                    rollos.size(),
                                                                    detalleConsolidado.getCntRollos());
                                                                return detalleConsolidado;
                                                            });
                                            });
                                })
                                .doOnSuccess(resultado -> {
                                    if (resultado != null) {
                                        log.debug("Detalle de ingreso procesado: ID={}, Rollos={}",
                                            resultado.getIdDetordeningreso(), resultado.getRollos().size());
                                    }
                                })
                                .doOnSuccess(resultado -> log.info(
                                    "Consulta de detalle de ingreso completada para partida ID: {} en almacén ID: {}",
                                    request.getIdPartida(), request.getIdAlmacen()))
                                .doOnError(error -> log.error(
                                    "Error consultando detalle de ingreso para partida ID {} en almacén ID {}: {}",
                                    request.getIdPartida(), request.getIdAlmacen(), error.getMessage())
                                );
        }
}
