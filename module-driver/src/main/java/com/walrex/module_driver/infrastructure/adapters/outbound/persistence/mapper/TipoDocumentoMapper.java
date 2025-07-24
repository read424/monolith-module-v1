package com.walrex.module_driver.infrastructure.adapters.outbound.persistence.mapper;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.dto.TipoDocumentoDTO;
import com.walrex.module_driver.infrastructure.adapters.outbound.persistence.entity.TipoDocumentoEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoDocumentoMapper {

    @Mapping(source = "abrevDoc", target = "abrevTipoDocumento")
    TipoDocumentoDTO toTipoDocumento(TipoDocumentoEntity tipoDocumentoEntity);
}
