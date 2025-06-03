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
public abstract class AgregarOrdenIngresoTelaCrudaService implements CrearOrdenIngresoUseCase {
    private final OrdenIngresoAdapterFactory adapterFactory;

    @Override
    public Mono<OrdenIngreso> crearOrdenIngresoLogistica(OrdenIngreso ordenIngreso) {
        log.info("Iniciando registro de orden de ingreso de tela cruda");

        // Aquí podríamos hacer validaciones o preprocesamiento específico para tela cruda

        // Usar siempre el adaptador de tela cruda
        return adapterFactory.getAdapter(TipoOrdenIngreso.TELA_CRUDA)
                .flatMap(adapter -> adapter.guardarOrdenIngresoLogistica(ordenIngreso));
    }

    @Override
    public Mono<OrdenIngresoResponseDTO> procesarMensajeOrdenIngreso(CreateOrdeningresoMessage message) {
        return null;
    }
}
