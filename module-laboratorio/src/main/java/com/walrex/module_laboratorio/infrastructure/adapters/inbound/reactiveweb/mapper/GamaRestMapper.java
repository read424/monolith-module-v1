package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_laboratorio.domain.model.Gama;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.GamaCreateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.GamaUpdateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.GamaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GamaRestMapper {

    Gama toDomain(GamaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Gama toDomain(GamaUpdateRequest request);

    GamaResponse toResponse(Gama domain);
}
