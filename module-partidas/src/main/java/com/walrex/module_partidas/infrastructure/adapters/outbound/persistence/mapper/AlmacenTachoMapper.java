package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.projection.AlmacenTachoProjection;

/**
 * Mapper para convertir entre AlmacenTachoProjection y AlmacenTacho
 * Utiliza MapStruct para generar el código de mapeo automáticamente
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlmacenTachoMapper {

    AlmacenTachoMapper INSTANCE = Mappers.getMapper(AlmacenTachoMapper.class);

    /**
     * Convierte AlmacenTachoProjection a AlmacenTacho
     * 
     * @param projection Proyección de la consulta SQL
     * @return Modelo de dominio
     */
    @Mapping(source = "idOrdeningreso", target = "idOrdeningreso")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "razonSocial", target = "razonSocial")
    @Mapping(source = "noAlias", target = "noAlias")
    @Mapping(source = "fecRegistro", target = "fecRegistro")
    @Mapping(source = "codIngreso", target = "codIngreso")
    @Mapping(source = "idDetordeningreso", target = "idDetordeningreso")
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "codPartida", target = "codPartida")
    @Mapping(source = "cntRollos", target = "cntRollos")
    @Mapping(source = "codReceta", target = "codReceta")
    @Mapping(source = "noColores", target = "noColores")
    @Mapping(source = "idTipoTenido", target = "idTipoTenido")
    @Mapping(source = "descTenido", target = "descTenido")
    AlmacenTacho toDomain(AlmacenTachoProjection projection);

    /**
     * Convierte AlmacenTacho a AlmacenTachoProjection
     * 
     * @param domain Modelo de dominio
     * @return Proyección para la consulta SQL
     */
    @Mapping(source = "idOrdeningreso", target = "idOrdeningreso")
    @Mapping(source = "idCliente", target = "idCliente")
    @Mapping(source = "razonSocial", target = "razonSocial")
    @Mapping(source = "noAlias", target = "noAlias")
    @Mapping(source = "fecRegistro", target = "fecRegistro")
    @Mapping(source = "codIngreso", target = "codIngreso")
    @Mapping(source = "idDetordeningreso", target = "idDetordeningreso")
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "codPartida", target = "codPartida")
    @Mapping(source = "cntRollos", target = "cntRollos")
    @Mapping(source = "codReceta", target = "codReceta")
    @Mapping(source = "noColores", target = "noColores")
    @Mapping(source = "idTipoTenido", target = "idTipoTenido")
    @Mapping(source = "descTenido", target = "descTenido")
    AlmacenTachoProjection toProjection(AlmacenTacho domain);
}
