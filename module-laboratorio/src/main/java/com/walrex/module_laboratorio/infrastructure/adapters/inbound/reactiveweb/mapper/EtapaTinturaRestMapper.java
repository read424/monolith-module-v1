package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_laboratorio.domain.model.EtapaTintura;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.EtapaTinturaRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.EtapaTinturaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EtapaTinturaRestMapper {
    EtapaTintura toDomain(EtapaTinturaRequest request);

    EtapaTinturaResponse toResponse(EtapaTintura domain);
}
