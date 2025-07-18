package com.walrex.module_almacen.infrastructure.adapters.inbound.consumer.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.avro.schemas.GuiaRemisionRemitenteData;
import com.walrex.module_almacen.domain.model.dto.ResponseEventGuiaDataDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionDataAvroMapper {

    GuiaRemisionDataAvroMapper INSTANCE = Mappers.getMapper(GuiaRemisionDataAvroMapper.class);

    @Mapping(source = "idOrdensalida", target = "idOrdenSalida")
    @Mapping(source = "idComprobante", target = "idComprobante")
    @Mapping(source = "codigoComprobante", target = "codigoComprobante")
    ResponseEventGuiaDataDto mapAvroToDto(GuiaRemisionRemitenteData message);

}
