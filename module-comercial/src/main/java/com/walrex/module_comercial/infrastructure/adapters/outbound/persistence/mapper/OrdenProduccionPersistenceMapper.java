package com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_comercial.domain.dto.OrdenProduccionRequestDTO;
import com.walrex.module_comercial.domain.dto.OrdenProduccionResponseDTO;
import com.walrex.module_comercial.domain.model.OrdenProduccion;
import com.walrex.module_comercial.infrastructure.adapters.outbound.persistence.entity.OrdenProduccionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Mapper MapStruct para conversiones entre modelo de dominio y entidad de persistencia de OrdenProduccion.
 * Pertenece a la capa de infraestructura (infrastructure layer).
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenProduccionPersistenceMapper {
    OrdenProduccionPersistenceMapper INSTANCE = Mappers.getMapper(OrdenProduccionPersistenceMapper.class);

    /**
     * Convierte un RequestDTO a modelo de dominio.
     */
    OrdenProduccion toDomain(OrdenProduccionRequestDTO dto);

    /**
     * Convierte un modelo de dominio a ResponseDTO.
     */
    OrdenProduccionResponseDTO toResponseDTO(OrdenProduccion domain);

    /**
     * Convierte un modelo de dominio a entidad de persistencia.
     */
    OrdenProduccionEntity toEntity(OrdenProduccion domain);

    /**
     * Convierte una entidad de persistencia a modelo de dominio.
     */
    OrdenProduccion toDomain(OrdenProduccionEntity entity);
}
