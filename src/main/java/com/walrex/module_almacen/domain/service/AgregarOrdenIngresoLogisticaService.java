package com.walrex.module_almacen.domain.service;

import com.walrex.avro.schemas.CreateOrdeningresoMessage;
import com.walrex.module_almacen.application.ports.input.CrearOrdenIngresoUseCase;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoResponseDTO;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgregarOrdenIngresoLogisticaService implements CrearOrdenIngresoUseCase {
    private final OrdenIngresoAdapterFactory adapterFactory;

    @Override
    public Mono<OrdenIngreso> crearOrdenIngresoLogistica(OrdenIngreso ordenIngresoDTO) {
        log.info("Iniciando registro de orden de ingreso");
        // Determinar qué tipo de orden es basado en propiedades de la orden
        TipoOrdenIngreso tipoOrden = determinarTipoOrden(ordenIngresoDTO);

        return adapterFactory.getAdapter(tipoOrden)
                .flatMap(adapter -> adapter.guardarOrdenIngresoLogistica(ordenIngresoDTO));
    }

    private TipoOrdenIngreso determinarTipoOrden(OrdenIngreso ordenIngreso) {
        // Lógica para determinar el tipo de orden basado en sus propiedades
        // Si tiene rollos, es tela cruda
        if (tieneDetallesConRollos(ordenIngreso)) {
            return TipoOrdenIngreso.TELA_CRUDA;
        }
        // Por defecto, considerarlo como logística general
        return TipoOrdenIngreso.LOGISTICA_GENERAL;
    }

    private boolean tieneDetallesConRollos(OrdenIngreso ordenIngreso) {
        if (ordenIngreso.getDetalles() == null || ordenIngreso.getDetalles().isEmpty()) {
            return false;
        }

        return ordenIngreso.getDetalles().stream()
                .anyMatch(detalle -> detalle.getDetallesRollos() != null && !detalle.getDetallesRollos().isEmpty());
    }

    @Override
    public Mono<OrdenIngresoResponseDTO> procesarMensajeOrdenIngreso(CreateOrdeningresoMessage message) {
        return null;
    }
}
