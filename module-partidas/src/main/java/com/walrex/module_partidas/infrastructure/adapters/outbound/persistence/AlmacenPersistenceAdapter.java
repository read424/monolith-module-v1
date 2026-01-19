package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import com.walrex.module_partidas.application.ports.output.AlmacenPort;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.PartidaInfoProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.RollsInStoreProjection;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.AlmacenesRepository;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.PartidaDetailsRepository;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.PartidaProcesosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Adaptador de persistencia para operaciones de almacén.
 * Implementa el puerto AlmacenPort y se comunica con los repositorios.
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlmacenPersistenceAdapter implements AlmacenPort {

    private final AlmacenesRepository almacenesRepository;
    private final PartidaDetailsRepository partidaDetailsRepository;
    private final PartidaProcesosRepository partidaProcesosRepository;

    @Override
    public Mono<PartidaInfoProjection> obtenerInfoPartida(Integer idPartida) {
        log.debug("Obteniendo información de partida ID: {}", idPartida);
        return partidaDetailsRepository.getInfoPartidaById(idPartida)
            .doOnSuccess(info -> log.info("Información de partida obtenida: idCliente={}, idArticulo={}, lote={}",
                info.getId_cliente(), info.getId_articulo(), info.getLote()))
            .doOnError(error -> log.error("Error obteniendo información de partida ID {}: {}",
                idPartida, error.getMessage()));
    }

    @Override
    public Flux<RollsInStoreProjection> obtenerRollosAlmacenados(Integer idPartida) {
        log.debug("Obteniendo rollos almacenados para partida ID: {}", idPartida);
        return partidaDetailsRepository.getRollsInStored(idPartida)
            .doOnNext(rollo -> log.debug("Rollo encontrado: codigo={}, peso={}, idDetOrdenIngresoPeso={}",
                rollo.getCodigo(), rollo.getPeso(), rollo.getIdDetOrdenIngresoPeso()))
            .doOnComplete(() -> log.info("Rollos almacenados obtenidos para partida ID: {}", idPartida))
            .doOnError(error -> log.error("Error obteniendo rollos almacenados para partida ID {}: {}",
                idPartida, error.getMessage()));
    }

    @Override
    public Mono<Integer> crearOrdenIngreso(Integer idCliente, Integer idOrigen, Integer idAlmacen) {
        log.debug("Creando orden de ingreso: idCliente={}, idOrigen={}, idAlmacen={}",
            idCliente, idOrigen, idAlmacen);
        return almacenesRepository.crearOrdenIngreso(idCliente, idOrigen, idAlmacen)
            .doOnSuccess(id -> log.info("Orden de ingreso creada con ID: {}", id))
            .doOnError(error -> log.error("Error creando orden de ingreso: {}", error.getMessage()));
    }

    @Override
    public Mono<Integer> crearDetalleOrdenIngreso(Integer idOrdenIngreso, Integer idArticulo, Integer idUnidad,
                                                   BigDecimal pesoRef, String lote, Integer nuRollos, Integer idComprobante) {
        log.debug("Creando detalle orden ingreso: idOrdenIngreso={}, idArticulo={}, nuRollos={}",
            idOrdenIngreso, idArticulo, nuRollos);
        return almacenesRepository.crearDetalleOrdenIngreso(
            idOrdenIngreso, idArticulo, idUnidad, pesoRef, lote, nuRollos, idComprobante)
            .doOnSuccess(id -> log.info("Detalle de orden de ingreso creado con ID: {}", id))
            .doOnError(error -> log.error("Error creando detalle de orden de ingreso: {}", error.getMessage()));
    }

    @Override
    public Mono<Integer> crearDetallePesoOrdenIngreso(Integer idOrdenIngreso, String codRollo, BigDecimal pesoRollo,
                                                       Integer idDetOrdenIngreso, Integer idRolloIngreso) {
        log.debug("Creando detalle peso: codRollo={}, peso={}", codRollo, pesoRollo);
        return almacenesRepository.crearDetallePesoOrdenIngreso(
            idOrdenIngreso, codRollo, pesoRollo, idDetOrdenIngreso, idRolloIngreso)
            .doOnSuccess(id -> log.info("Detalle de peso creado con ID: {} para rollo: {}", id, codRollo))
            .doOnError(error -> log.error("Error creando detalle de peso para rollo {}: {}",
                codRollo, error.getMessage()));
    }

    @Override
    public Mono<Boolean> cambiarStatusRollo(Integer idDetOrdenIngresoPeso, Integer status) {
        log.debug("Cambiando status de rollo ID: {} a status: {}", idDetOrdenIngresoPeso, status);
        return partidaProcesosRepository.changeStatusRollInStore(idDetOrdenIngresoPeso, status)
            .doOnSuccess(result -> log.info("Status de rollo ID {} cambiado a {}: resultado={}",
                idDetOrdenIngresoPeso, status, result))
            .doOnError(error -> log.error("Error cambiando status de rollo ID {}: {}",
                idDetOrdenIngresoPeso, error.getMessage()));
    }
}
