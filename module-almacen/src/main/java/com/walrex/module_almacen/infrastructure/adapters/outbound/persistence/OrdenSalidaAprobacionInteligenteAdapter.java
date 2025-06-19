package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaAprobacionPort;
import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.ArticuloRequerimiento;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TypeMotivoEgreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class OrdenSalidaAprobacionInteligenteAdapter implements OrdenSalidaAprobacionPort {
    private final OrdenSalidaAprobacionPort aprobacionNormalAdapter;
    private final OrdenSalidaAprobacionPort aprobacionMovimientoAdapter;
    private final OrdenSalidaRepository ordenSalidaRepository;

    public OrdenSalidaAprobacionInteligenteAdapter(
            @Qualifier("aprobacionSalida") OrdenSalidaAprobacionPort aprobacionNormalAdapter,
            @Qualifier("aprobacionMovimiento") OrdenSalidaAprobacionPort aprobacionMovimientoAdapter,
            OrdenSalidaRepository ordenSalidaRepository) {

        this.aprobacionNormalAdapter = aprobacionNormalAdapter;
        this.aprobacionMovimientoAdapter = aprobacionMovimientoAdapter;
        this.ordenSalidaRepository = ordenSalidaRepository;
    }

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> procesarAprobacionCompleta(
            AprobarSalidaRequerimiento request,
            List<ArticuloRequerimiento> productosSeleccionados) {

        // 1️⃣ Consultar orden para obtener motivo
        return consultarYValidarOrdenParaAprobacion(request.getIdOrdenSalida())
                .flatMap(ordenCompleta -> {
                    // 2️⃣ Decidir qué adapter usar según el motivo
                    if (ordenCompleta.getMotivo().getIdMotivo().equals(TypeMotivoEgreso.MOVIMIENTO_ALMACEN.getId())) {
                        log.info("🔄 Procesando como movimiento entre almacenes");
                        return aprobacionMovimientoAdapter.procesarAprobacionCompleta(request, productosSeleccionados);
                    } else {
                        log.info("📦 Procesando como entrega normal");
                        return aprobacionNormalAdapter.procesarAprobacionCompleta(request, productosSeleccionados);
                    }
                });
    }

    @Override
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        return aprobacionNormalAdapter.guardarOrdenSalida(ordenSalida);
    }

    @Override
    public Mono<OrdenEgresoDTO> consultarYValidarOrdenParaAprobacion(Integer idOrdenSalida) {
        return aprobacionNormalAdapter.consultarYValidarOrdenParaAprobacion(idOrdenSalida);
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(OrdenEgresoDTO ordenEgresoDTO) {
        return aprobacionNormalAdapter.actualizarEstadoEntrega(ordenEgresoDTO);
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        return aprobacionNormalAdapter.procesarSalidaPorLotes(ordenSalida);
    }
}
