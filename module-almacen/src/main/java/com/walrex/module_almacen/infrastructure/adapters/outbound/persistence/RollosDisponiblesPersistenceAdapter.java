package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_almacen.application.ports.output.ConsultarRollosDisponiblesPort;
import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.RolloDisponibleDevolucionMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleRolloRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Adapter de persistencia para consultar rollos disponibles para devolución
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RollosDisponiblesPersistenceAdapter implements ConsultarRollosDisponiblesPort {

    private final DetalleRolloRepository detalleRolloRepository;
    private final RolloDisponibleDevolucionMapper rolloMapper;

    @Override
    public Flux<RolloDisponibleDevolucionDTO> buscarRollosDisponibles(Integer idCliente, Integer idArticulo) {
        log.info("🔍 Consultando rollos disponibles en BD - Cliente: {}, Artículo: {}", idCliente, idArticulo);

        return detalleRolloRepository.buscarRollosDisponiblesParaDevolucion(idCliente, idArticulo)
                .map(rolloMapper::projectionToDto)
                .filter(this::esAlmacenPermitidoParaDevolucion)
                .doOnNext(rollo -> log.debug("✅ Rollo disponible encontrado en BD (almacén permitido): {} - Almacén: {}", 
                    rollo.getCodRollo(), rollo.getIdIngresoAlmacen()))
                .doOnError(error -> log.error("❌ Error al consultar rollos disponibles en BD: {}", error.getMessage()))
                .onErrorMap(throwable -> new RuntimeException("Error al consultar rollos disponibles", throwable));
    }

    /**
     * Valida si el rollo pertenece a un almacén permitido para devolución
     * Almacenes permitidos: 2 (Tela Cruda), 10 (Almacén de Devolución), 32 (Almacén de Preparado)
     */
    private boolean esAlmacenPermitidoParaDevolucion(RolloDisponibleDevolucionDTO rollo) {
        if (rollo.getIdIngresoAlmacen() == null) {
            log.warn("⚠️ Rollo {} sin ID de almacén, se excluye de devolución", rollo.getCodRollo());
            return false;
        }

        Integer idAlmacen = rollo.getIdIngresoAlmacen();
        boolean esPermitido = idAlmacen.equals(2) || idAlmacen.equals(10) || idAlmacen.equals(32);

        if (!esPermitido) {
            log.debug("🚫 Rollo {} del almacén {} no está permitido para devolución", 
                rollo.getCodRollo(), idAlmacen);
        }

        return esPermitido;
    }
}