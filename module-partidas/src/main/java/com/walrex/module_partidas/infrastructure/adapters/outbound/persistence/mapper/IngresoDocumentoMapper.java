package com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.dto.IngresoDocumentoDTO;
import com.walrex.module_partidas.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoDocumentoEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngresoDocumentoMapper {
    IngresoDocumentoMapper INSTANCE = Mappers.getMapper(IngresoDocumentoMapper.class);


    @Mapping(source = "id_ordeningreso", target = "idOrdenIngreso")
    @Mapping(source = "id_tipo_documento", target = "idTipoDocumento")
    @Mapping(source = "id_documento", target = "idDocumento")
    @Mapping(source = "id_almacen", target = "idAlmacen")
    OrdenIngresoDocumentoEntity toEntity(IngresoDocumentoDTO ingresoDocumentoDTO);
}
