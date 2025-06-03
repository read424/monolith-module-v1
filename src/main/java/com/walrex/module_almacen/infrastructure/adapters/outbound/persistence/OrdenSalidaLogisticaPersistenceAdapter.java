package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailSalidaRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class OrdenSalidaLogisticaPersistenceAdapter implements OrdenSalidaLogisticaPort {
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final DetailSalidaRepository detalleSalidaRepository;
    private final OrdenSalidaEntityMapper ordenSalidaEntityMapper;
    private final DetailSalidaMapper detailSalidaMapper;

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        // Implementación básica
        return Mono.just(ordenSalida);
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(Integer idOrden, boolean entregado) {
        // Implementación básica
        return Mono.empty();
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        return null;
    }

    @Override
    public Mono<OrdenEgresoDTO> consultarYValidarOrdenParaAprobacion(Integer idOrdenSalida) {
        return null;
    }
}
