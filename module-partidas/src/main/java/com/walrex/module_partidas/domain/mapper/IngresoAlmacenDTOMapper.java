package com.walrex.module_partidas.domain.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.IngresoAlmacen;
import com.walrex.module_partidas.domain.model.dto.IngresoAlmacenDTO;

/**
 * Mapper para convertir IngresoAlmacen a IngresoAlmacenDTO
 * Maneja la conversión de rollos y el mapeo de ingresos
 *
 * @author Ronald E. Aybar D.
 * @version 0.0.1-SNAPSHOT
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngresoAlmacenDTOMapper {

    IngresoAlmacenDTOMapper INSTANCE = Mappers.getMapper(IngresoAlmacenDTOMapper.class);

    /**
     * Convierte IngresoAlmacen a IngresoAlmacenDTO con mapeo de ingresos
     */
    @Mapping(source = "idOrdeningreso", target = "idOrdeningreso")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "codIngreso", target = "codIngreso")
    @Mapping(source = "idAlmacen", target = "idAlmacen")
    @Mapping(source = "idArticulo", target = "idArticulo")
    @Mapping(source = "idUnidad", target = "idUnidad")
    @Mapping(source = "cntRollos", target = "cntRollos")
    @Mapping(source = "pesoRef", target = "pesoRef")
    @Mapping(source = "rollos", target = "rollos")
    @Mapping(target = "ingresos", ignore = true)
    IngresoAlmacenDTO toIngresoAlmacenDTO(IngresoAlmacen ingresoAlmacen);
}
