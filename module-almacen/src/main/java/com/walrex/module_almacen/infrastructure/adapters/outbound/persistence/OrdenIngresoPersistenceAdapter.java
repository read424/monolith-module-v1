package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoPersistencePort;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class OrdenIngresoPersistenceAdapter implements OrdenIngresoPersistencePort {
    private final OrdenIngresoRepository ordenIngresoRepository;

    @Override
    public Mono<OrdenIngresoDTO> guardarOrdenIngreso(OrdenIngresoDTO ordenIngresoDTO) {
        return ordenIngresoRepository.agregarIngreso(
                        ordenIngresoDTO.getIdMotivo(),
                        ordenIngresoDTO.getObservacion(),
                        ordenIngresoDTO.getFechaIngreso(),
                        ordenIngresoDTO.getIdAlmacen()
                )
                .map(this::mapToDto);
    }

    @Override
    public Mono<OrdenIngresoDTO> buscarOrdenIngresoPorId(Long id) {
        return ordenIngresoRepository.findById(id)
                .map(this::mapToDto);
    }

    private OrdenIngresoDTO mapToDto(OrdenIngresoEntity entity) {
        return OrdenIngresoDTO.builder()
                .id(entity.getId())
                .idMotivo(entity.getId_motivo())
                .idAlmacen(entity.getId_almacen())
                .observacion(entity.getObservacion())
                .fechaIngreso(entity.getFec_ingreso())
                .build();
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

}
