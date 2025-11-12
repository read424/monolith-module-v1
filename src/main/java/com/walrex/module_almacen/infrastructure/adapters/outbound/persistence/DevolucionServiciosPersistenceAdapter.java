package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import org.springframework.stereotype.Component;

import com.walrex.module_almacen.application.ports.output.DevolucionServiciosPersistencePort;
import com.walrex.module_almacen.domain.model.dto.ResponseEventGuiaDataDto;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DevolucionServiciosRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DevolucionServiciosPersistenceAdapter implements DevolucionServiciosPersistencePort {
    private final DevolucionServiciosRepository devolucionServiciosRepository;

    @Override
    public Mono<Void> actualizarIdComprobante(ResponseEventGuiaDataDto data, String comprobanteId) {
        return devolucionServiciosRepository
                .actualizarIdComprobante(data.getIdComprobante(), data.getIdOrdenSalida())
                .doOnNext(updatedRows -> {
                    if (updatedRows > 0) {
                        log.info("✅ Registro actualizado correctamente. OrdenSalida: {}, Comprobante: {}",
                                data.getIdOrdenSalida(), data.getIdComprobante());
                    } else {
                        log.warn("⚠️ No se encontró el registro para actualizar. OrdenSalida: {}, Comprobante: {}",
                                data.getIdOrdenSalida(), data.getIdComprobante());
                    }
                })
                .doOnError(error -> {
                    log.error("❌ Error al actualizar el comprobante. OrdenSalida: {}, Comprobante: {}. Error: {}",
                            data.getIdOrdenSalida(), data.getIdComprobante(), error.getMessage());
                    throw new IllegalArgumentException("Error en la capa de persistencia", error);
                })
                .then();
    }
}
