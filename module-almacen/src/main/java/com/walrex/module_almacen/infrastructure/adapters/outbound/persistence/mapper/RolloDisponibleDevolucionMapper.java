package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.RolloDisponibleDevolucionProjection;

/**
 * Mapper para convertir entre Projection y DTO de dominio usando MapStruct
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolloDisponibleDevolucionMapper {

    RolloDisponibleDevolucionMapper INSTANCE = Mappers.getMapper(RolloDisponibleDevolucionMapper.class);

    /**
     * Convierte una projection de persistencia a DTO de dominio
     */
    @Mapping(source = "codIngreso", target = "codigoOrdenIngreso")
    @Mapping(source = "nuComprobante", target = "numComprobante")
    @Mapping(source = "statusIng", target = "statusRolloIngreso")
    @Mapping(source = "statusAlmacen", target = "statusRolloAlmacen")
    @Mapping(source = "idAlmacen", target = "idIngresoAlmacen")
    @Mapping(source = "idDetPartida", target = "idDetallePartida")
    RolloDisponibleDevolucionDTO projectionToDto(RolloDisponibleDevolucionProjection projection);
}
