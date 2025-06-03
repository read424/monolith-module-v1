package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenEgresoTransformacionMapper {
    OrdenEgresoTransformacionMapper INSTANCE = Mappers.getMapper(OrdenEgresoTransformacionMapper.class);

    @Mapping(source = "fec_ingreso", target = "fecRegistro")
    OrdenEgresoDTO toOrdenEgreso(OrdenIngresoTransformacionDTO dto);
}
