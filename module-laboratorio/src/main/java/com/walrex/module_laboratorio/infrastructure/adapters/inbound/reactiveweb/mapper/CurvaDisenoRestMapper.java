package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.walrex.module_laboratorio.domain.model.CurvaDiseno;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.request.CurvaDisenoCreateRequest;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.CurvaDisenoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CurvaDisenoRestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "curvaDiseno", expression = "java(toJsonString(request.getCurvaDiseno()))")
    @Mapping(target = "idLaboratorista", ignore = true)
    @Mapping(target = "laboratorista", ignore = true)
    @Mapping(target = "idSupervisor", ignore = true)
    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CurvaDiseno toDomain(CurvaDisenoCreateRequest request);

    CurvaDisenoResponse toResponse(CurvaDiseno domain);

    default String toJsonString(JsonNode jsonNode) {
        return jsonNode == null ? null : jsonNode.toString();
    }
}
