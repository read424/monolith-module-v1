package com.walrex.module_comercial.domain.mapper;

import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioRequestDTO;
import com.walrex.module_comercial.domain.dto.SolicitudCambioDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper MapStruct para conversiones relacionadas con SolicitudCambio en la capa de dominio.
 * Se genera automáticamente en tiempo de compilación.
 *
 * Responsabilidad: Transformaciones internas entre DTOs de dominio.
 * Pertenece a la capa de dominio (domain layer).
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SolicitudCambioMapper {

    SolicitudCambioMapper INSTANCE = Mappers.getMapper(SolicitudCambioMapper.class);

    /**
     * Crea una copia del DTO de SolicitudCambio.
     * Útil para transformaciones o enriquecimientos de datos.
     *
     * @param dto DTO original
     * @return Nueva instancia del DTO
     */
    SolicitudCambioDTO copy(SolicitudCambioDTO dto);

    /**
     * Crea una copia del DTO de GuardarSolicitudCambioRequest.
     * Útil para operaciones que requieren inmutabilidad.
     *
     * @param dto DTO original
     * @return Nueva instancia del DTO
     */
    GuardarSolicitudCambioRequestDTO copy(GuardarSolicitudCambioRequestDTO dto);

    /**
     * Extrae el objeto SolicitudCambioDTO desde GuardarSolicitudCambioRequestDTO.
     * Útil para procesamiento de datos específicos.
     *
     * @param request DTO de request completo
     * @return Solo el objeto SolicitudCambioDTO
     */
    @Mapping(target = "codOrdenproduccion", source = "solicitudCambio.codOrdenproduccion")
    @Mapping(target = "descArticulo", source = "solicitudCambio.descArticulo")
    @Mapping(target = "idGama", source = "solicitudCambio.idGama")
    @Mapping(target = "idOrdenproduccion", source = "solicitudCambio.idOrdenproduccion")
    @Mapping(target = "idRuta", source = "solicitudCambio.idRuta")
    @Mapping(target = "precio", source = "solicitudCambio.precio")
    SolicitudCambioDTO extractSolicitudCambio(GuardarSolicitudCambioRequestDTO request);
}