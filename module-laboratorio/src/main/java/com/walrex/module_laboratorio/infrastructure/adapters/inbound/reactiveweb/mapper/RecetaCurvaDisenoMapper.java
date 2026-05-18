package com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_laboratorio.domain.model.CurvaDisenoItem;
import com.walrex.module_laboratorio.domain.model.Receta;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.CurvaDisenoItemResponse;
import com.walrex.module_laboratorio.infrastructure.adapters.inbound.reactiveweb.dto.response.RecetaCurvaDisenoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecetaCurvaDisenoMapper {
    RecetaCurvaDisenoResponse toResponse(Receta receta);
    CurvaDisenoItemResponse toItemResponse(CurvaDisenoItem item);
}
