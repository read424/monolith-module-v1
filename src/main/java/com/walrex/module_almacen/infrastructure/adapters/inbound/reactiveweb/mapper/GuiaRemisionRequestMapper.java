package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.request.GenerarGuiaRemisionRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionRequestMapper {

    @Mapping(source = "idMotivoTraslado", target = "idMotivoComprobante")
    GuiaRemisionGeneradaDTO toDomainDTO(GenerarGuiaRemisionRequest request);

    GenerarGuiaRemisionRequest toResponseDTO(GuiaRemisionGeneradaDTO domain);
}