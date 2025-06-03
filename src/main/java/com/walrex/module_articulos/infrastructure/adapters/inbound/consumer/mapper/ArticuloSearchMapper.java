package com.walrex.module_articulos.infrastructure.adapters.inbound.consumer.mapper;

import com.walrex.module_articulos.domain.model.ArticuloSearchCriteria;
import com.walrex.module_articulos.infrastructure.adapters.inbound.reactiveweb.dto.RequestSearchDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticuloSearchMapper {
    ArticuloSearchMapper INSTANCE = Mappers.getMapper(ArticuloSearchMapper.class);

    @Mapping(source = "search", target = "query")
    RequestSearchDTO ArticuloSearchToDTO (ArticuloSearchCriteria articuloSearchCriteria);

    // De DTO a dominio (te falta este)
    @Mapping(source = "query", target = "search")
    ArticuloSearchCriteria dtoToArticuloSearch(RequestSearchDTO requestSearchDTO);
}
