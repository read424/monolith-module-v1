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
     * Aplica validaciones para evitar duplicación de IDs
     */
    @Mapping(source = "codIngreso", target = "codigoOrdenIngreso")
    @Mapping(source = "nuComprobante", target = "numComprobante")
    @Mapping(source = "statusIng", target = "statusRolloIngreso")
    @Mapping(source = "statusAlmacen", target = "statusRolloAlmacen")
    @Mapping(source = "idAlmacen", target = "idIngresoAlmacen")
    @Mapping(source = "idDetPartida", target = "idDetallePartida")
    RolloDisponibleDevolucionDTO projectionToDto(RolloDisponibleDevolucionProjection projection);

    /**
     * Post-procesamiento para aplicar validaciones de campos duplicados
     * Si los IDs del almacén son iguales a los IDs originales, se setean a NULL
     */
    @AfterMapping
    default void aplicarValidacionCamposDuplicados(@MappingTarget RolloDisponibleDevolucionDTO dto, 
                                                   RolloDisponibleDevolucionProjection projection) {
        if (dto == null) {
            return;
        }

        // Si id_ordeningreso_almacen == id_ordeningreso, setear id_ordeningreso_almacen a NULL
        if (dto.getIdOrdeningresoAlmacen() != null && 
            dto.getIdOrdeningreso() != null && 
            dto.getIdOrdeningresoAlmacen().equals(dto.getIdOrdeningreso())) {
            dto.setIdOrdeningresoAlmacen(null);
        }

        // Si id_detordeningreso_almacen == id_detordeningreso, setear id_detordeningreso_almacen a NULL
        if (dto.getIdDetordeningresoAlmacen() != null && 
            dto.getIdDetordeningreso() != null && 
            dto.getIdDetordeningresoAlmacen().equals(dto.getIdDetordeningreso())) {
            dto.setIdDetordeningresoAlmacen(null);
        }

        // Si id_detordeningresopeso_almacen == id_detordeningresopeso, setear id_detordeningresopeso_almacen a NULL
        if (dto.getIdDetordeningresopesoAlmacen() != null && 
            dto.getIdDetordeningresopeso() != null && 
            dto.getIdDetordeningresopesoAlmacen().equals(dto.getIdDetordeningresopeso())) {
            dto.setIdDetordeningresopesoAlmacen(null);
        }
    }
}
