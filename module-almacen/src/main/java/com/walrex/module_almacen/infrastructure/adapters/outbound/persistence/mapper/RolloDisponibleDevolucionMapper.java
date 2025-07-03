package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.walrex.module_almacen.domain.model.dto.RolloDisponibleDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.RolloDisponibleDevolucionProjection;

/**
 * Mapper para convertir entre Projection y DTO de dominio usando MapStruct
 * Sigue el patrón Mapper Pattern separando las capas
 */
@Mapper(componentModel = "spring")
public interface RolloDisponibleDevolucionMapper {

    RolloDisponibleDevolucionMapper INSTANCE = Mappers.getMapper(RolloDisponibleDevolucionMapper.class);

    /**
     * Convierte una projection de persistencia a DTO de dominio
     * MapStruct automáticamente mapea campos con el mismo nombre
     */
    @Mapping(source = "codIngreso", target = "codigoOrdenIngreso")
    @Mapping(source = "nuComprobante", target = "numComprobante")
    @Mapping(source = "statusIng", target = "statusRolloIngreso")
    @Mapping(source = "statusAlmacen", target = "statusRolloAlmacen")
    @Mapping(source = "idDetPartida", target = "idDetallePartida")
    @Mapping(source = "idAlmacen", target = "idIngresoAlmacen")
    RolloDisponibleDevolucionDTO projectionToDto(RolloDisponibleDevolucionProjection projection);
}