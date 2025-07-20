package com.walrex.module_almacen.infrastructure.adapters.inbound.consumer.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.avro.schemas.GuiaRemisionRemitenteResponse;
import com.walrex.module_almacen.domain.model.dto.GuiaRemisionResponseEventDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
        GuiaRemisionDataAvroMapper.class })
public interface GuiaRemisionResponseAvroMapper {
    GuiaRemisionResponseAvroMapper INSTANCE = Mappers.getMapper(GuiaRemisionResponseAvroMapper.class);

    @Mapping(source = "success", target = "success")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "data", target = "data")
    GuiaRemisionResponseEventDTO mapAvroToDto(GuiaRemisionRemitenteResponse message);
}
