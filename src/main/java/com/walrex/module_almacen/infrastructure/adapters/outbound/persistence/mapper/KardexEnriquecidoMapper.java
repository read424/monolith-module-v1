package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.KardexDetalleEnriquecido;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.projection.KardexDetalleProjection;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KardexEnriquecidoMapper {

    KardexDetalleEnriquecido toEnriquecido(KardexDetalleProjection base);
}
