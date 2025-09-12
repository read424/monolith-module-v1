package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.mapper;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.BuscarConductorModel;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request.SearchConductorRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchDriverRequestMapper {

    @Mapping(source = "numDoc", target = "numeroDocumento")
    @Mapping(source = "idTipDoc", target = "tipoDocumento.idTipoDocumento")
    @Mapping(source = "name", target = "nombres")
    BuscarConductorModel toDomain(SearchConductorRequest request);

}
