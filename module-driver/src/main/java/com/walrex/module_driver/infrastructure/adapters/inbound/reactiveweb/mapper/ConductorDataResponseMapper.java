package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.mapper;

import java.util.List;

import org.mapstruct.*;

import com.walrex.module_driver.domain.model.dto.ConductorDataDTO;
import com.walrex.module_driver.domain.model.dto.TipoDocumentoDTO;
import com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.response.ConductorResponse;

/**
 * Mapper para convertir entre ConductorDataDTO y ConductorResponse usando
 * MapStruct.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConductorDataResponseMapper {

    /**
     * Convierte ConductorDataDTO a ConductorResponse.
     */
    @Mapping(source = "numeroDocumento", target = "numeroDocumento")
    @Mapping(source = "apellidos", target = "apellidos")
    @Mapping(source = "nombres", target = "nombres")
    @Mapping(source = "numLicencia", target = "licencia")
    @Mapping(source = "tipoDocumento", target = "tipoDocumento")
    ConductorResponse toResponse(ConductorDataDTO conductorDataDTO);

    /**
     * Convierte ConductorResponse a ConductorDataDTO.
     */
    @InheritInverseConfiguration
    ConductorDataDTO toConductorDataDTO(ConductorResponse conductorResponse);

    /**
     * Mapea TipoDocumentoDTO a TipoDocumentoResponse.
     */
    @Mapping(source = "idTipoDocumento", target = "idTipoDocumento")
    @Mapping(source = "descTipoDocumento", target = "descTipoDocumento")
    @Mapping(source = "abrevTipoDocumento", target = "abrevTipoDocumento")
    ConductorResponse.TipoDocumentoResponse toTipoDocumentoResponse(TipoDocumentoDTO tipoDocumentoDTO);

    /**
     * Mapea TipoDocumentoResponse a TipoDocumentoDTO.
     */
    @InheritInverseConfiguration
    TipoDocumentoDTO toTipoDocumentoDTO(ConductorResponse.TipoDocumentoResponse tipoDocumentoResponse);

    /**
     * Mapea List<ConductorDataDTO> a List<ConductorResponse>.
     */
    List<ConductorResponse> toResponseList(List<ConductorDataDTO> dtos);
}