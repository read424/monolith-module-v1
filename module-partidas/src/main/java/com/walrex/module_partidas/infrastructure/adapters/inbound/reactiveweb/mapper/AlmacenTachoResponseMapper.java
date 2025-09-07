package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.mapper;

import java.util.List;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_partidas.domain.model.dto.AlmacenTachoResponseDTO;
import com.walrex.module_partidas.domain.model.dto.PartidaTachoResponse;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.ListPartidaTachoResponse;
import com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.response.ResponsePartidaTacho;

/**
 * Mapper para convertir entre DTOs de respuesta de almacén tacho
 * Convierte desde el dominio hacia la capa de infraestructura
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlmacenTachoResponseMapper {

    AlmacenTachoResponseMapper INSTANCE = Mappers.getMapper(AlmacenTachoResponseMapper.class);

    /**
     * Mapea de AlmacenTachoResponseDTO (dominio) a ListPartidaTachoResponse (infraestructura)
     * 
     * @param dto Response DTO del dominio
     * @return Response de infraestructura
     */
    @Mapping(source = "partidas", target = "partidas")
    @Mapping(source = "totalRecords", target = "totalRecords")
    @Mapping(source = "totalPages", target = "totalPages")
    @Mapping(source = "currentPage", target = "currentPage")
    @Mapping(source = "pageSize", target = "pageSize")
    @Mapping(source = "hasNext", target = "hasNext")
    @Mapping(source = "hasPrevious", target = "hasPrevious")
    ListPartidaTachoResponse toListPartidaTachoResponse(AlmacenTachoResponseDTO dto);

    /**
     * Mapea un elemento individual de PartidaTachoResponse del dominio a ResponsePartidaTacho
     * 
     * @param partidaTacho Partida del dominio
     * @return Partida de infraestructura
     */
    ResponsePartidaTacho toResponsePartidaTacho(PartidaTachoResponse partidaTacho);

    /**
     * Mapea una lista de PartidaTachoResponse del dominio a List<ResponsePartidaTacho>
     * MapStruct automáticamente mapeará cada elemento usando toResponsePartidaTacho
     * 
     * @param partidaTacho Lista de partidas del dominio
     * @return Lista de partidas de infraestructura
     */
    List<ResponsePartidaTacho> listToResponse(List<PartidaTachoResponse> partidaTacho);
}
