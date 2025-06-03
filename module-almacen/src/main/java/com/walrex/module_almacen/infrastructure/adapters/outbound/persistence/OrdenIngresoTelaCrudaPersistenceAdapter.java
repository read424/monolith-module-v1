package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.common.Exception.OrdenIngresoException;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.DetalleRollo;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetalleRolloRepository;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@SuperBuilder
@Slf4j
public class OrdenIngresoTelaCrudaPersistenceAdapter extends BaseOrdenIngresoPersistenceAdapter {
    protected final DetalleRolloRepository detalleRolloRepository;

    @Override
    protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
            DetalleOrdenIngreso detalle,
            DetailsIngresoEntity savedDetalleEntity,
            OrdenIngreso ordenIngreso) {
        // Verificar que haya rollos (opcional, según tu lógica de negocio)
        if (detalle.getDetallesRollos() == null || detalle.getDetallesRollos().isEmpty()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los detalles de tela cruda deben incluir al menos un rollo"
            ));
        }

        // Guardar rollos y actualizar detalle
        return guardarDetallesRollos(
                detalle.getDetallesRollos(),
                savedDetalleEntity.getId(),
                savedDetalleEntity.getId_ordeningreso()
        )
                .collectList()
                .flatMap(rollosGuardados -> {
                    // Actualizar el ID del detalle
                    detalle.setId(savedDetalleEntity.getId().intValue());
                    // Actualizar rollos
                    detalle.setDetallesRollos(rollosGuardados);
                    return Mono.just(detalle);
                });
    }

    // Método para guardar los detalles de rollos
    protected Flux<DetalleRollo> guardarDetallesRollos(List<DetalleRollo> rollos, Long idDetalle, Long idOrdenIngreso) {
        return Flux.fromIterable(rollos)
                .flatMap(rollo -> {
                    DetalleRolloEntity rolloEntity = DetalleRolloEntity.builder()
                            .codRollo(rollo.getCodRollo())
                            .pesoRollo(rollo.getPesoRollo())
                            .idDetOrdenIngreso(idDetalle.intValue())
                            .ordenIngreso(idOrdenIngreso.intValue())
                            .build();

                    return detalleRolloRepository.save(rolloEntity)
                            .doOnSuccess(savedEntity ->
                                    log.debug("Rollo guardado con ID: {}", savedEntity.getId())
                            )
                            .map(savedRolloEntity -> {
                                rollo.setId(savedRolloEntity.getId());
                                rollo.setIdDetOrdenIngreso(idDetalle.intValue());
                                rollo.setOrdenIngreso(idOrdenIngreso.intValue());
                                return rollo;
                            })
                            .onErrorResume(ex -> {
                                String errorMsg = "Error al guardar rollo " + rollo.getCodRollo();
                                log.error(errorMsg, ex);
                                return Mono.error(new OrdenIngresoException(errorMsg, ex));
                            });
                });
    }
}
