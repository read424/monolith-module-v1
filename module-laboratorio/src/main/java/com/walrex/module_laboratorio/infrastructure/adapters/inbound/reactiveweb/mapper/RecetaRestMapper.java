package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_laboratorio.domain.model.Receta;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.RecetaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecetaRestMapper {
    RecetaResponse toResponse(Receta receta);
}
