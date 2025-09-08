package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.walrex.module_partidas.application.ports.output.OrdenSalidaPersistencePort;
import com.walrex.module_partidas.domain.exceptions.OrdenSalidaPersistenceException;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaMovimientoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrdenSalidaPersistenceAdapter implements OrdenSalidaPersistencePort {

    private final OrdenSalidaMovimientoRepository ordenSalidaRepository;

    @Override
    public Mono<Integer> crearOrdenSalida(
            Integer idAlmacenOrigen,
            Integer idAlmacenDestino,
            OffsetDateTime fecRegistro,
            Integer idUsuario,
            Integer idDocumentoRef
    ) {
        return ordenSalidaRepository.insertOrdenSalida(
                idAlmacenOrigen,
                idAlmacenDestino,
                fecRegistro,
                idUsuario,
                idDocumentoRef
        )
        .onErrorMap(throwable -> new OrdenSalidaPersistenceException(
            "Error al crear orden de salida: " + throwable.getMessage(), throwable));
    }

    @Override
    public Mono<Integer> crearDetalleOrdenSalida(
            Integer idOrdenSalida,
            Integer idArticulo,
            Integer idUnidad,
            Integer cantidad,
            Integer idPartida,
            BigDecimal totKilos,
            Integer idDetOrdenIngreso
    ) {
        return ordenSalidaRepository.insertDetalleOrdenSalida(
                idOrdenSalida,
                idArticulo,
                idUnidad,
                cantidad,
                idPartida,
                totKilos,
                idDetOrdenIngreso
        )
        .onErrorMap(throwable -> new OrdenSalidaPersistenceException(
            "Error al crear detalle de orden de salida: " + throwable.getMessage(), throwable));
    }

    @Override
    public Mono<Void> crearDetOrdenSalidaPeso(
            Integer idDetalleOrden,
            Integer idOrdenSalida,
            String codRollo,
            BigDecimal pesoRollo,
            Integer idDetPartida,
            Integer idRolloIngreso
    ) {
        return ordenSalidaRepository.insertDetOrdenSalidaPeso(
                idDetalleOrden,
                idOrdenSalida,
                codRollo,
                pesoRollo,
                idDetPartida,
                idRolloIngreso
        )
        .onErrorMap(throwable -> new OrdenSalidaPersistenceException(
            "Error al crear detalle de peso de orden de salida: " + throwable.getMessage(), throwable));
    }
}
