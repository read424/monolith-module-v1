package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.dto.MotivoDevolucionDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.response.MotivoDevolucionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper para convertir MotivoDevolucionDTO a MotivoDevolucionResponse
 */
@Mapper(componentModel = "spring")
public interface MotivoDevolucionResponseMapper {

    MotivoDevolucionResponseMapper INSTANCE = Mappers.getMapper(MotivoDevolucionResponseMapper.class);

    /**
     * Convierte un DTO de dominio a response de API
     * Excluye campos internos como fechas de auditoría
     * 
     * @param motivoDevolucionDTO DTO del dominio
     * @return Response para la API
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "status", source = "status")
    MotivoDevolucionResponse toResponse(MotivoDevolucionDTO motivoDevolucionDTO);

    /**
     * Convierte una lista de DTOs de dominio a lista de responses de API
     * 
     * @param motivosDevolucionDTO Lista de DTOs del dominio
     * @return Lista de responses para la API
     */
    List<MotivoDevolucionResponse> toResponseList(List<MotivoDevolucionDTO> motivosDevolucionDTO);
} 