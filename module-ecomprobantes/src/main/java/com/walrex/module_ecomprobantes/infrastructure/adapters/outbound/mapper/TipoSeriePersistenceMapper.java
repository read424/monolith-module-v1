package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.walrex.module_ecomprobantes.domain.model.dto.TipoSerieDTO;
import com.walrex.module_ecomprobantes.domain.model.entity.TipoSerieEntity;

/**
 * Mapper para convertir entre TipoSerieEntity y TipoSerieDTO.
 * 
 * Utiliza MapStruct para generar automáticamente el código de mapeo.
 */
@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TipoSeriePersistenceMapper {

    /**
     * Convierte TipoSerieEntity a TipoSerieDTO
     * 
     * @param entity Entidad a convertir
     * @return DTO convertido
     */
    TipoSerieDTO toDTO(TipoSerieEntity entity);

    /**
     * Convierte TipoSerieDTO a TipoSerieEntity
     * 
     * @param dto DTO a convertir
     * @return Entidad convertida
     */
    TipoSerieEntity toEntity(TipoSerieDTO dto);

    /**
     * Convierte lista de TipoSerieEntity a lista de TipoSerieDTO
     * 
     * @param entities Lista de entidades a convertir
     * @return Lista de DTOs convertidos
     */
    List<TipoSerieDTO> toDTOList(List<TipoSerieEntity> entities);

    /**
     * Convierte lista de TipoSerieDTO a lista de TipoSerieEntity
     * 
     * @param dtos Lista de DTOs a convertir
     * @return Lista de entidades convertidas
     */
    List<TipoSerieEntity> toEntityList(List<TipoSerieDTO> dtos);
}