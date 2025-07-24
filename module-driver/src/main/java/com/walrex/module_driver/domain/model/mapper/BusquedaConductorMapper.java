package com.walrex.module_driver.domain.model.mapper;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.BuscarConductorModel;
import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusquedaConductorMapper {
    @Mapping(source = "tipoDocumento.idTipoDocumento", target = "tipoDocumento.idTipoDocumento")
    @Mapping(source = "tipoDocumento.descTipoDocumento", target = "tipoDocumento.descTipoDocumento")
    @Mapping(source = "tipoDocumento.abrevTipoDocumento", target = "tipoDocumento.abrevTipoDocumento")
    @Mapping(source = "numeroDocumento", target = "numeroDocumento")
    @Mapping(source = "apellidos", target = "apellidos")
    @Mapping(source = "nombres", target = "nombres")
    @Mapping(source = "numLicencia", target = "numLicencia")
    ConductorDataDTO toConductorDataDTO(BuscarConductorModel buscarConductorModel);

    @InheritInverseConfiguration
    BuscarConductorModel toBuscarConductorModel(ConductorDataDTO conductorDataDTO);
}
