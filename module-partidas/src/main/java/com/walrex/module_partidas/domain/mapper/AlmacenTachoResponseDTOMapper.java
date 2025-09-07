package com.walrex.module_partidas.domain.mapper;

import java.util.List;

import org.mapstruct.*;

import com.walrex.module_partidas.domain.model.AlmacenTacho;
import com.walrex.module_partidas.domain.model.AlmacenTachoResponse;
import com.walrex.module_partidas.domain.model.dto.AlmacenTachoResponseDTO;
import com.walrex.module_partidas.domain.model.dto.PartidaTachoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlmacenTachoResponseDTOMapper {

    /**
     * Convierte de AlmacenTachoResponse (dominio) a AlmacenTachoResponseDTO (API)
     * 
     * @param response Response del dominio
     * @return DTO para la API
     */
    @Mapping(source = "almacenes", target = "partidas", qualifiedByName = "mapAlmacenesToPartidas")
    @Mapping(source = "totalRecords", target = "totalRecords")
    @Mapping(source = "totalPages", target = "totalPages")
    @Mapping(source = "currentPage", target = "currentPage")
    @Mapping(source = "pageSize", target = "pageSize")
    @Mapping(source = "hasNext", target = "hasNext")
    @Mapping(source = "hasPrevious", target = "hasPrevious")
    AlmacenTachoResponseDTO toDTO(AlmacenTachoResponse response);

    /**
     * Mapeo personalizado de List<AlmacenTacho> a List<PartidaTachoResponse>
     * 
     * @param almacenes Lista de almacenes del dominio
     * @return Lista de partidas para el DTO
     */
    @Named("mapAlmacenesToPartidas")
    List<PartidaTachoResponse> mapAlmacenesToPartidas(List<AlmacenTacho> almacenes);

    /**
     * Convierte un AlmacenTacho individual a PartidaTachoResponse
     * Este método debe ser implementado según la estructura de tus clases
     * 
     * @param almacen Almacén del dominio
     * @return Partida para el DTO
     */
    @Mapping(source = "idPartida", target = "idPartida")
    @Mapping(source = "codPartida", target = "codPartida")
    // Agregar aquí todos los mappings necesarios según los campos de PartidaTachoResponse
    PartidaTachoResponse almacenToPartida(AlmacenTacho almacen);    
}
