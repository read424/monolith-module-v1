package com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_comercial.domain.dto.GuardarSolicitudCambioRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.walrex.module_comercial.domain.dto.SolicitudCambioDTO;
import com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.request.GuardarSolicitudCambioRequest;
import com.walrex.module_comercial.infrastructure.adapters.inbound.reactiveweb.request.SolicitudCambioRequest;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper MapStruct para convertir Request DTOs (infrastructure) a Domain DTOs.
 * Se genera automáticamente en tiempo de compilación.
 *
 * Responsabilidad: Traducir datos de entrada HTTP al lenguaje del dominio.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SolicitudCambioRequestMapper {

    SolicitudCambioRequestMapper INSTANCE = Mappers.getMapper(SolicitudCambioRequestMapper.class);

    /**
     * Convierte el request DTO a DTO de dominio para GuardarSolicitudCambio.
     *
     * @param request Request HTTP desde la capa de infrastructure
     * @return DTO de dominio
     */
    @Mapping(target = "aplicarOtrasPartidas", source = "aplicarOtrasPartidas")
    @Mapping(target = "cntPartidas", source = "cntPartidas")
    @Mapping(target = "idOrdenproduccion", source = "idOrdenproduccion")
    @Mapping(target = "idPartida", source = "idPartida")
    @Mapping(target = "isDelivered", source = "isDelivered")
    @Mapping(target = "solicitudCambio", source = "solicitudCambio")
    GuardarSolicitudCambioRequestDTO toGuardarSolicitudCambioDTO(
            GuardarSolicitudCambioRequest request);

    /**
     * Convierte el request DTO anidado a DTO de dominio.
     *
     * @param request Request HTTP anidado
     * @return DTO de dominio
     */
    @Mapping(target = "idOrdenproduccion", source = "idOrdenproduccion")
    @Mapping(target = "idRuta", source = "idRuta")
    @Mapping(target = "codOrdenproduccion", source = "codOrdenproduccion")
    @Mapping(target = "descArticulo", source = "descArticulo")
    @Mapping(target = "idGama", source = "idGama")
    @Mapping(target = "idPrecio", source = "idPrecio")
    @Mapping(target = "precio", source = "precio")
    @Mapping(target = "idOrden", source = "idOrden")
    @Mapping(target = "idDetOs", source = "idDetOs")
    SolicitudCambioDTO toSolicitudCambioDTO(SolicitudCambioRequest request);
}
