package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.*;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DevolucionServiciosEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionEntityMapper {
    /**
     * Actualiza una entidad existente con los datos de la guía de remisión
     * 
     * @param dto    Datos de la guía de remisión
     * @param entity Entidad existente a actualizar
     * @return Entidad actualizada
     */
    @Mapping(source = "idDevolucion", target = "id")
    @Mapping(target = "createAt", ignore = true) // Campo manejado automáticamente por BD
    @Mapping(target = "updateAt", ignore = true) // Campo manejado automáticamente por BD
    DevolucionServiciosEntity DTOtoEntity(
            GuiaRemisionGeneradaDTO dto);

    /**
     * Mapea entidad a DTO para respuesta
     */
    @InheritInverseConfiguration
    GuiaRemisionGeneradaDTO entityToDTO(DevolucionServiciosEntity entity);
}