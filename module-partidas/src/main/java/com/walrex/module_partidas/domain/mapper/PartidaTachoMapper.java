package com.walrex.module_partidas.domain.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.domain.model.dto.PartidaTachoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartidaTachoMapper {

    PartidaTachoMapper INSTANCE = Mappers.getMapper(PartidaTachoMapper.class);

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
    PartidaTachoResponse toDTOAlmacenTachoResponse(AlmacenTacho almacenTacho);
}
