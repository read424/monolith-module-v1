package com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_articulos.domain.model.dto.ArticuloDto;
import com.walrex.module_articulos.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticuloEntityMapper {
    ArticuloEntityMapper INSTANCE = Mappers.getMapper(ArticuloEntityMapper.class);

    @Mapping(source = "id", target = "id_articulo")
    @Mapping(source = "create_at", target="fec_ingreso")
    ArticuloDto entityToDto(ArticuloEntity entity);

    @Named("offsetDateTimeToLocalDateTime")
    static LocalDateTime offsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
